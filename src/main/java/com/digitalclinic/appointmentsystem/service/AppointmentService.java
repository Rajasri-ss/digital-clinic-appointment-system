package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.*;
import com.digitalclinic.appointmentsystem.model.*;
import com.digitalclinic.appointmentsystem.repository.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ClinicLocationRepository locationRepository;

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;

    @Autowired
    private PatientVitalRepository patientVitalRepository;

    @Autowired
    private LabTestRepository labTestRepository;

    @Autowired
    private ModelMapper modelMapper;

    public AppointmentDTO bookAppointment(Long patientUserId, AppointmentRequestDTO requestDTO) {
        logger.info("Booking appointment for patient user ID: {}", patientUserId);

        validateBooking(requestDTO);

        Patient patient = patientRepository.findByUser_Id(patientUserId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        Doctor doctor = doctorRepository.findById(requestDTO.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        ClinicLocation location = locationRepository.findById(requestDTO.getLocationId())
                .orElseThrow(() -> new RuntimeException("Clinic Location not found"));

        if (!requestDTO.isEmergency()) {
            boolean isSlotTaken = !checkSlotAvailability(doctor.getId(), requestDTO.getAppointmentDate(),
                    requestDTO.getAppointmentTime());

            if (isSlotTaken) {
                throw new RuntimeException("This time slot is already booked. Please select another time.");
            }
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setLocation(location);
        appointment.setAppointmentDate(requestDTO.getAppointmentDate());
        appointment.setAppointmentTime(requestDTO.getAppointmentTime());
        appointment.setReasonForVisit(requestDTO.getReasonForVisit());
        appointment.setIsEmergency(requestDTO.isEmergency());

        try {
            if (requestDTO.getConsultationType() != null) {
                appointment.setConsultationType(
                        Appointment.ConsultationType.valueOf(requestDTO.getConsultationType().toUpperCase()));
            }
            if (requestDTO.getUrgencyLevel() != null) {
                appointment
                        .setUrgencyLevel(Appointment.UrgencyLevel.valueOf(requestDTO.getUrgencyLevel().toUpperCase()));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid enum value provided during booking");
        }

        appointment.setConsultationFee(
                doctor.getConsultationFee() != null ? doctor.getConsultationFee() : BigDecimal.ZERO);

        if (requestDTO.isEmergency()) {
            appointment.setUrgencyLevel(Appointment.UrgencyLevel.CRITICAL);
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToDTO(savedAppointment);
    }

    public void validateBooking(AppointmentRequestDTO requestDTO) {
        if (requestDTO.getAppointmentDate() != null && requestDTO.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot book appointment in the past.");
        }
    }

    public AppointmentDTO bookEmergencyAppointment(Long patientUserId, EmergencyBookingDTO requestDTO) {
        logger.info("Booking emergency appointment for patient user ID: {}", patientUserId);

        if (requestDTO.getAppointmentDate() != null && requestDTO.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot book emergency appointment in the past.");
        }

        Patient patient = patientRepository.findByUser_Id(patientUserId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        Doctor doctor = doctorRepository.findById(requestDTO.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        ClinicLocation location = locationRepository.findById(requestDTO.getLocationId())
                .orElseThrow(() -> new RuntimeException("Clinic Location not found"));

        LocalTime startTime = requestDTO.getAppointmentTime().withMinute(0).withSecond(0).withNano(0);
        LocalTime endTime = startTime.plusHours(1);

        long overlappingCount = appointmentRepository
                .countByDoctorIdAndAppointmentDateAndAppointmentTimeBetweenAndStatusNot(
                        doctor.getId(),
                        requestDTO.getAppointmentDate(),
                        startTime,
                        endTime,
                        Appointment.AppointmentStatus.CANCELLED);

        if (overlappingCount >= 6) {
            throw new RuntimeException("Maximum hourly emergency capacity reached. Please try the next hour.");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setLocation(location);

        LocalTime allocatedTime = handleEmergencySlotAllocation(requestDTO.getUrgencyLevel(), doctor.getId(),
                requestDTO.getAppointmentDate());
        appointment.setAppointmentDate(requestDTO.getAppointmentDate());
        appointment.setAppointmentTime(allocatedTime != null ? allocatedTime : requestDTO.getAppointmentTime());

        appointment.setReasonForVisit(requestDTO.getReasonForVisit());
        appointment.setIsEmergency(true);
        appointment.setChiefComplaint(requestDTO.getChiefComplaint());
        appointment.setSymptoms(requestDTO.getSymptoms());

        try {
            if (requestDTO.getConsultationType() != null) {
                appointment.setConsultationType(
                        Appointment.ConsultationType.valueOf(requestDTO.getConsultationType().toUpperCase()));
            } else {
                appointment.setConsultationType(Appointment.ConsultationType.IN_PERSON);
            }
            if (requestDTO.getUrgencyLevel() != null) {
                appointment
                        .setUrgencyLevel(Appointment.UrgencyLevel.valueOf(requestDTO.getUrgencyLevel().toUpperCase()));
            } else {
                appointment.setUrgencyLevel(Appointment.UrgencyLevel.CRITICAL);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid enum value provided during emergency booking");
        }

        appointment.setConsultationFee(
                doctor.getConsultationFee() != null ? doctor.getConsultationFee() : BigDecimal.ZERO);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToDTO(savedAppointment);
    }

    public LocalTime handleEmergencySlotAllocation(String urgencyLevel, Long doctorId, LocalDate date) {
        if (date == null)
            return null;
        if (date.isEqual(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            return now.plusMinutes(15).withSecond(0).withNano(0); // Next 15 minutes roughly
        }
        return LocalTime.of(9, 0); // Default to start of day
    }

    public boolean checkSlotAvailability(Long doctorId, LocalDate date, LocalTime time) {
        return !appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                doctorId, date, time, Appointment.AppointmentStatus.CANCELLED);
    }

    public List<AvailableSlotDTO> getAvailableSlots(Long doctorId, Long locationId, LocalDate date) {
        logger.info("Fetching available slots for doctor: {}, location: {}, date: {}", doctorId, locationId, date);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Get the doctor's schedule for this day
        DoctorSchedule.DayOfWeek dayOfWeek = DoctorSchedule.DayOfWeek.valueOf(date.getDayOfWeek().name());
        logger.debug("Day of week for date {}: {}", date, dayOfWeek);

        // Find ACTIVE schedule by doctor, location, and day of week
        DoctorSchedule schedule = doctorScheduleRepository.findActiveScheduleByDoctorLocationAndDay(
                doctorId, locationId, dayOfWeek)
                .orElseThrow(() -> new RuntimeException("No active schedule found for doctor at this location on " + dayOfWeek));

        logger.debug("Schedule found: start={}, end={}, slotDuration={}",
                schedule.getStartTime(), schedule.getEndTime(), schedule.getSlotDuration());

        // Check effective date range
        if (schedule.getEffectiveFrom() != null && date.isBefore(schedule.getEffectiveFrom())) {
            throw new RuntimeException("This schedule is not yet effective");
        }
        if (schedule.getEffectiveTo() != null && date.isAfter(schedule.getEffectiveTo())) {
            throw new RuntimeException("This schedule has expired");
        }

        // Get booked appointments for this doctor, location, and date (excluding cancelled)
        List<Appointment> bookedAppointments = appointmentRepository
                .findBookedSlots(doctorId, locationId, date);

        logger.debug("Found {} booked appointments for doctor {} at location {} on date {}",
                bookedAppointments.size(), doctorId, locationId, date);
        bookedAppointments.forEach(app ->
                logger.debug("  - Booked slot: time={}, status={}", app.getAppointmentTime(), app.getStatus()));

        // Create a set of booked times
        java.util.Set<LocalTime> bookedTimes = bookedAppointments.stream()
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toSet());

        logger.debug("Booked times: {}", bookedTimes);

        // Generate slots based on doctor's schedule
        List<AvailableSlotDTO> slots = new java.util.ArrayList<>();
        LocalTime currentTime = schedule.getStartTime();
        LocalTime endTime = schedule.getEndTime();
        int slotDuration = schedule.getSlotDuration();

        while (currentTime.isBefore(endTime)) {
            boolean isBooked = bookedTimes.contains(currentTime);
            slots.add(AvailableSlotDTO.builder()
                    .date(date)
                    .startTime(currentTime)
                    .endTime(currentTime.plusMinutes(slotDuration))
                    .isAvailable(!isBooked)
                    .isEmergencySlot(false) // Emergency slots handled separately
                    .build());
            currentTime = currentTime.plusMinutes(slotDuration);
        }

        logger.info("Generated {} slots ({} available) for doctor {} at location {} on date {}",
                slots.size(), slots.stream().filter(AvailableSlotDTO::isAvailable).count(),
                doctorId, locationId, date);
        return slots;
    }

    private DoctorSchedule createDefaultSchedule(Doctor doctor, Long locationId, DoctorSchedule.DayOfWeek dayOfWeek) {
        logger.info("Creating default schedule for doctor: {}, location: {}, day: {}", doctor.getId(), locationId, dayOfWeek);

        ClinicLocation location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctor(doctor);
        schedule.setLocation(location);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(LocalTime.of(9, 0)); // 9:00 AM
        schedule.setEndTime(LocalTime.of(17, 0)); // 5:00 PM
        schedule.setSlotDuration(30); // 30 minute slots
        schedule.setActive(true);

        return doctorScheduleRepository.save(schedule);
    }

    public AppointmentDTO rescheduleAppointment(Long appointmentId, LocalDate newDate, LocalTime newTime,
            Long patientUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatient().getUser().getId().equals(patientUserId)) {
            throw new RuntimeException("Unauthorized to reschedule this appointment");
        }

        if (appointment.getStatus() != Appointment.AppointmentStatus.SCHEDULED) {
            throw new RuntimeException("Only SCHEDULED appointments can be rescheduled");
        }

        boolean isSlotTaken = !checkSlotAvailability(appointment.getDoctor().getId(), newDate, newTime);

        if (isSlotTaken) {
            throw new RuntimeException("The new time slot is already booked.");
        }

        appointment.setAppointmentDate(newDate);
        appointment.setAppointmentTime(newTime);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToDTO(savedAppointment);
    }

    public Page<AppointmentDTO> getPatientAppointments(Long patientUserId, int page, int size) {
        logger.info("Fetching appointments for patient user ID: {}", patientUserId);
        Patient patient = patientRepository.findByUser_Id(patientUserId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointments = appointmentRepository
                .findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patient.getId(), pageable);

        return appointments.map(this::convertToDTO);
    }

    public Page<AppointmentDTO> getDoctorAppointments(Long doctorUserId, int page, int size) {
        logger.info("Fetching appointments for doctor user ID: {}", doctorUserId);
        Doctor doctor = doctorRepository.findByUser_Id(doctorUserId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointments = appointmentRepository
                .findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(doctor.getId(), pageable);

        return appointments.map(this::convertToDTO);
    }

    public Page<AppointmentDTO> getDoctorAppointments(Long doctorUserId, int page, int size, LocalDate date) {
        logger.info("Fetching appointments for doctor user ID: {} on date: {}", doctorUserId, date);
        Doctor doctor = doctorRepository.findByUser_Id(doctorUserId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentTime").ascending());
        Page<Appointment> appointments;
        if (date != null) {
            appointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctor.getId(), date, pageable);
        } else {
            appointments = appointmentRepository.findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(doctor.getId(), pageable);
        }

        return appointments.map(this::convertToDTO);
    }

    public AppointmentDetailsDTO getAppointmentDetailsForDoctor(Long appointmentId, Long doctorUserId) {
        logger.info("Fetching appointment details for doctor. ID: {}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new RuntimeException("Unauthorized to view this appointment");
        }

        Patient patient = appointment.getPatient();
        int age = 0;
        if (patient.getDateOfBirth() != null) {
            age = java.time.Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears();
        }

        PatientShortDTO patientDTO = PatientShortDTO.builder()
                .patientId(patient.getId())
                .name(patient.getFullName())
                .age(age)
                .gender(patient.getGender())
                .bloodGroup(patient.getBloodGroup())
                .build();

        return AppointmentDetailsDTO.builder()
                .appointmentId(appointment.getId())
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus().name())
                .patient(patientDTO)
                .symptoms(appointment.getSymptoms())
                .build();
    }

    public void recordVitals(Long appointmentId, Long doctorUserId, PatientVitalRequest request) {
        logger.info("Recording vitals for appointment: {}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new RuntimeException("Unauthorized");
        }

        PatientVital vital = PatientVital.builder()
                .patient(appointment.getPatient())
                .bloodPressure(request.getBloodPressure())
                .heartRate(request.getHeartRate())
                .temperature(request.getTemperature())
                .weight(request.getWeight())
                .height(request.getHeight())
                .oxygenSaturation(request.getOxygenSaturation())
                .recordedBy(appointment.getDoctor().getUser())
                .build();

        if (request.getWeight() != null && request.getHeight() != null && request.getHeight() > 0) {
            double heightInMeters = request.getHeight() / 100.0;
            double bmi = request.getWeight() / (heightInMeters * heightInMeters);
            vital.setNotes("Automatically calculated BMI: " + String.format("%.2f", bmi));
        }

        patientVitalRepository.save(vital);
    }

    public void updateNotes(Long appointmentId, Long doctorUserId, AppointmentNotesRequest request) {
        logger.info("Updating notes for appointment: {}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new RuntimeException("Unauthorized");
        }

        appointment.setDiagnosis(request.getDiagnosis());
        appointment.setDoctorNotes(request.getDoctorNotes());
        appointmentRepository.save(appointment);
    }

    public void orderLabTest(Long appointmentId, Long doctorUserId, LabTestRequest request) {
        logger.info("Ordering lab test for appointment: {}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new RuntimeException("Unauthorized");
        }

        LabTest labTest = LabTest.builder()
                .patient(appointment.getPatient())
                .doctor(appointment.getDoctor())
                .testName(request.getTestName())
                .status("PENDING")
                .build();

        labTestRepository.save(labTest);
    }

    public List<AppointmentDTO> getDoctorDailySchedule(Long doctorUserId, LocalDate date) {
        logger.info("Fetching daily schedule for doctor user ID: {} on date: {}", doctorUserId, date);
        Doctor doctor = doctorRepository.findByUser_Id(doctorUserId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        return appointmentRepository.findDoctorAppointmentsForDate(doctor.getId(), date)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AppointmentDTO getAppointmentDetails(Long appointmentId, Long userId, String role) {
        logger.info("Fetching appointment details ID: {}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Basic Authorization check
        if ("PATIENT".equals(role)) {
            if (!appointment.getPatient().getUser().getId().equals(userId)) {
                throw new RuntimeException("Unauthorized to view this appointment");
            }
        } else if ("DOCTOR".equals(role)) {
            if (!appointment.getDoctor().getUser().getId().equals(userId)) {
                throw new RuntimeException("Unauthorized to view this appointment");
            }
        }

        return convertToDTO(appointment);
    }

    public AppointmentDTO updateAppointmentStatus(Long appointmentId, Long doctorUserId, String newStatus) {
        logger.info("Updating appointment {} status to {} by doctor {}", appointmentId, newStatus, doctorUserId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new RuntimeException("Unauthorized to update this appointment");
        }

        try {
            Appointment.AppointmentStatus status = Appointment.AppointmentStatus.valueOf(newStatus.toUpperCase());
            appointment.setStatus(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status provided");
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToDTO(savedAppointment);
    }

    public AppointmentDTO updateAppointment(Long appointmentId, Long doctorUserId,
            com.digitalclinic.appointmentsystem.dto.AppointmentUpdateRequestDTO requestDTO) {
        logger.info("Updating appointment {} by doctor {}", appointmentId, doctorUserId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new RuntimeException("Unauthorized to update this appointment");
        }

        // Update diagnosis
        if (requestDTO.getDiagnosis() != null && !requestDTO.getDiagnosis().trim().isEmpty()) {
            appointment.setDiagnosis(requestDTO.getDiagnosis().trim());
        }

        // Update doctor notes
        if (requestDTO.getDoctorNotes() != null) {
            appointment.setDoctorNotes(requestDTO.getDoctorNotes().trim());
        }

        // Update status
        if (requestDTO.getStatus() != null && !requestDTO.getStatus().trim().isEmpty()) {
            try {
                Appointment.AppointmentStatus status = Appointment.AppointmentStatus.valueOf(
                        requestDTO.getStatus().toUpperCase());
                appointment.setStatus(status);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status provided: " + requestDTO.getStatus());
            }
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        logger.info("Appointment {} updated successfully", appointmentId);
        return convertToDTO(savedAppointment);
    }

    public AppointmentDTO cancelAppointment(Long appointmentId, Long patientUserId) {
        logger.info("Cancelling appointment {} by patient {}", appointmentId, patientUserId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatient().getUser().getId().equals(patientUserId)) {
            throw new RuntimeException("Unauthorized to cancel this appointment");
        }

        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED ||
                appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel an appointment that is already " + appointment.getStatus());
        }

        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToDTO(savedAppointment);
    }

    private AppointmentDTO convertToDTO(Appointment appointment) {
        AppointmentDTO dto = modelMapper.map(appointment, AppointmentDTO.class);

        dto.setAppointmentId(appointment.getId());
        dto.setPatientId(appointment.getPatient().getId());
        dto.setPatientName(appointment.getPatient().getFullName());
        dto.setPatientGender(appointment.getPatient().getGender());
        dto.setPatientBloodGroup(appointment.getPatient().getBloodGroup());
        dto.setPatientDateOfBirth(appointment.getPatient().getDateOfBirth());
        if (appointment.getPatient().getUser() != null) {
            dto.setPatientPhone(appointment.getPatient().getUser().getPhone());
        }

        dto.setDoctorId(appointment.getDoctor().getId());
        dto.setDoctorName(appointment.getDoctor().getFullName());
        dto.setDoctorSpecialization(appointment.getDoctor().getSpecialization());

        dto.setLocationId(appointment.getLocation().getId());
        dto.setClinicName(appointment.getLocation().getClinicName());
        dto.setClinicAddress(appointment.getLocation().getAddress());

        if (appointment.getConsultationType() != null)
            dto.setConsultationType(appointment.getConsultationType().name());
        if (appointment.getStatus() != null)
            dto.setStatus(appointment.getStatus().name());
        if (appointment.getUrgencyLevel() != null)
            dto.setUrgencyLevel(appointment.getUrgencyLevel().name());
        if (appointment.getPaymentStatus() != null)
            dto.setPaymentStatus(appointment.getPaymentStatus().name());

        return dto;
    }
}
