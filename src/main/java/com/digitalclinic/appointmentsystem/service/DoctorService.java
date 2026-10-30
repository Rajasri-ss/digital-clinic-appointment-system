package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.AppointmentDTO;
import com.digitalclinic.appointmentsystem.dto.DashboardMetricsDTO;
import com.digitalclinic.appointmentsystem.dto.DoctorDashboardDTO;
import com.digitalclinic.appointmentsystem.dto.DoctorProfileDTO;
import com.digitalclinic.appointmentsystem.exception.ResourceNotFoundException;
import com.digitalclinic.appointmentsystem.model.Appointment;
import com.digitalclinic.appointmentsystem.repository.AppointmentRepository;
import com.digitalclinic.appointmentsystem.dto.DoctorSearchDTO;
import com.digitalclinic.appointmentsystem.dto.DoctorScheduleDTO;
import com.digitalclinic.appointmentsystem.model.Doctor;
import com.digitalclinic.appointmentsystem.model.DoctorLocation;
import com.digitalclinic.appointmentsystem.model.DoctorSchedule;
import com.digitalclinic.appointmentsystem.model.ClinicLocation;
import com.digitalclinic.appointmentsystem.repository.DoctorRepository;
import com.digitalclinic.appointmentsystem.repository.DoctorLocationRepository;
import com.digitalclinic.appointmentsystem.repository.DoctorScheduleRepository;
import com.digitalclinic.appointmentsystem.repository.ClinicLocationRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorService.class);

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorLocationRepository doctorLocationRepository;

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;

    @Autowired
    private ClinicLocationRepository clinicLocationRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ModelMapper modelMapper;

    public DoctorDashboardDTO getDashboardData(Long userId) {
        logger.info("Fetching dashboard data for doctor user ID: {}", userId);
        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        LocalDate today = LocalDate.now();

        List<AppointmentDTO> todayAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(doctor.getId(), today)
                .stream()
                .map(this::convertToAppointmentDTO)
                .collect(Collectors.toList());

        List<AppointmentDTO> emergencyAppointments = appointmentRepository
                .findEmergencyAppointments(doctor.getId(), today)
                .stream()
                .map(this::convertToAppointmentDTO)
                .collect(Collectors.toList());

        DashboardMetricsDTO metrics = DashboardMetricsDTO.builder()
                .todayAppointments(appointmentRepository.countByDoctorIdAndAppointmentDate(doctor.getId(), today))
                .totalPatients(appointmentRepository.countDistinctPatientsByDoctorId(doctor.getId()))
                .pendingAppointments(appointmentRepository.countByDoctorIdAndStatus(doctor.getId(), Appointment.AppointmentStatus.SCHEDULED))
                .completedAppointments(appointmentRepository.countByDoctorIdAndStatus(doctor.getId(), Appointment.AppointmentStatus.COMPLETED))
                .build();

        return DoctorDashboardDTO.builder()
                .todayAppointments(todayAppointments)
                .emergencyAppointments(emergencyAppointments)
                .metrics(metrics)
                .build();
    }

    private AppointmentDTO convertToAppointmentDTO(Appointment appointment) {
        AppointmentDTO dto = modelMapper.map(appointment, AppointmentDTO.class);
        dto.setAppointmentId(appointment.getId());
        dto.setPatientId(appointment.getPatient().getId());
        dto.setPatientName(appointment.getPatient().getFullName());
        if (appointment.getPatient().getUser() != null) {
            dto.setPatientPhone(appointment.getPatient().getUser().getPhone());
        }
        dto.setDoctorId(appointment.getDoctor().getId());
        dto.setDoctorName(appointment.getDoctor().getFullName());
        if (appointment.getConsultationType() != null)
            dto.setConsultationType(appointment.getConsultationType().name());
        if (appointment.getStatus() != null)
            dto.setStatus(appointment.getStatus().name());
        if (appointment.getUrgencyLevel() != null)
            dto.setUrgencyLevel(appointment.getUrgencyLevel().name());
        return dto;
    }

    public Page<DoctorProfileDTO> searchDoctors(DoctorSearchDTO searchDTO) {
        logger.info("Searching doctors with filters");

        Sort sort = Sort.by(Sort.Direction.DESC, "averageRating");
        if ("fee".equalsIgnoreCase(searchDTO.getSortBy())) {
            sort = Sort.by(Sort.Direction.ASC, "consultationFee");
        } else if ("experience".equalsIgnoreCase(searchDTO.getSortBy())) {
            sort = Sort.by(Sort.Direction.DESC, "experienceYears");
        }

        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);

        Page<Doctor> doctors = doctorRepository.searchDoctors(
                searchDTO.getSpecialization(),
                searchDTO.getMinExperience(),
                searchDTO.getMaxFee(),
                pageable);

        return doctors.map(this::convertToProfileDTO);
    }

    public DoctorProfileDTO getDoctorProfile(Long doctorId) {
        logger.info("Fetching doctor profile ID: {}", doctorId);
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));
        return convertToProfileDTO(doctor);
    }

    public DoctorProfileDTO getMyProfile(Long userId) {
        logger.info("Fetching doctor profile for user ID: {}", userId);
        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
        return convertToProfileDTO(doctor);
    }

    public DoctorProfileDTO updateDoctorProfile(Long userId, DoctorProfileDTO dto) {
        logger.info("Updating doctor profile for user ID: {}", userId);
        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        doctor.setFullName(dto.getFullName());
        doctor.setSpecialization(dto.getSpecialization());
        doctor.setQualifications(dto.getQualifications());
        doctor.setExperienceYears(dto.getExperienceYears());
        doctor.setLicenseNumber(dto.getLicenseNumber());
        doctor.setAbout(dto.getAbout());
        doctor.setLanguagesSpoken(dto.getLanguagesSpoken());
        doctor.setConsultationFee(dto.getConsultationFee());
        doctor.setProfilePhoto(dto.getProfilePhoto());

        if (dto.getIsAvailable() != null && !dto.getIsAvailable().equals(doctor.getAvailable())) {
            doctor.setIsAvailable(dto.getIsAvailable());
        }

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return convertToProfileDTO(updatedDoctor);
    }

    public void addClinicLocation(Long userId, Long locationId, BigDecimal fee, boolean isPrimary) {
        logger.info("Adding clinic location {} for doctor user ID {}", locationId, userId);
        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        ClinicLocation location = clinicLocationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        DoctorLocation doctorLocation = DoctorLocation.builder()
                .doctor(doctor)
                .location(location)
                .consultationFee(fee)
                .isPrimary(isPrimary)
                .build();

        doctorLocationRepository.save(doctorLocation);
    }

    public void removeClinicLocation(Long userId, Long locationId) {
        logger.info("Removing clinic location {} for doctor user ID {}", locationId, userId);
        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        DoctorLocation doctorLocation = doctorLocationRepository.findByDoctorIdAndLocationId(doctor.getId(), locationId)
                .orElseThrow(() -> new RuntimeException("DoctorLocation not found"));
        doctorLocationRepository.delete(doctorLocation);
    }

    public List<com.digitalclinic.appointmentsystem.dto.ClinicLocationDTO> getDoctorClinics(Long userId) {
        logger.info("Fetching clinics for doctor user ID: {}", userId);
        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<DoctorLocation> doctorLocations = doctorLocationRepository.findByDoctorId(doctor.getId());
        return doctorLocations.stream()
                .map(dl -> convertToClinicLocationDTO(dl.getLocation(), dl.getConsultationFee()))
                .collect(Collectors.toList());
    }

    public List<com.digitalclinic.appointmentsystem.dto.ClinicLocationDTO> getDoctorClinicsByDoctorId(Long doctorId) {
        logger.info("Fetching clinics for doctor ID: {}", doctorId);
        List<DoctorLocation> doctorLocations = doctorLocationRepository.findByDoctorId(doctorId);
        return doctorLocations.stream()
                .map(dl -> convertToClinicLocationDTO(dl.getLocation(), dl.getConsultationFee()))
                .collect(Collectors.toList());
    }

    public com.digitalclinic.appointmentsystem.dto.ClinicLocationDTO addClinic(Long userId, com.digitalclinic.appointmentsystem.dto.ClinicLocationDTO dto) {
        logger.info("Adding new clinic for doctor user ID: {}", userId);
        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Create new clinic location
        ClinicLocation clinic = ClinicLocation.builder()
                .clinicName(dto.getClinicName())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .pincode(dto.getPincode())
                .phone(dto.getPhone())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .facilities(dto.getFacilities())
                .operatingHours(dto.getOperatingHours())
                .build();

        ClinicLocation savedClinic = clinicLocationRepository.save(clinic);

        // Link doctor to clinic
        DoctorLocation doctorLocation = DoctorLocation.builder()
                .doctor(doctor)
                .location(savedClinic)
                .consultationFee(dto.getConsultationFeeAtThisLocation() != null ?
                        dto.getConsultationFeeAtThisLocation() : doctor.getConsultationFee())
                .isPrimary(false)
                .build();

        doctorLocationRepository.save(doctorLocation);

        return convertToClinicLocationDTO(savedClinic, doctorLocation.getConsultationFee());
    }

    public com.digitalclinic.appointmentsystem.dto.ClinicLocationDTO updateClinic(Long userId, Long clinicId, com.digitalclinic.appointmentsystem.dto.ClinicLocationDTO dto) {
        logger.info("Updating clinic ID: {} for doctor user ID: {}", clinicId, userId);
        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Verify doctor has access to this clinic
        DoctorLocation doctorLocation = doctorLocationRepository.findByDoctorIdAndLocationId(doctor.getId(), clinicId)
                .orElseThrow(() -> new RuntimeException("Clinic not found or unauthorized"));

        ClinicLocation clinic = doctorLocation.getLocation();
        clinic.setClinicName(dto.getClinicName());
        clinic.setAddress(dto.getAddress());
        clinic.setCity(dto.getCity());
        clinic.setState(dto.getState());
        clinic.setPincode(dto.getPincode());
        clinic.setPhone(dto.getPhone());
        clinic.setLatitude(dto.getLatitude());
        clinic.setLongitude(dto.getLongitude());
        clinic.setFacilities(dto.getFacilities());
        clinic.setOperatingHours(dto.getOperatingHours());

        ClinicLocation updatedClinic = clinicLocationRepository.save(clinic);

        // Update consultation fee if provided
        if (dto.getConsultationFeeAtThisLocation() != null) {
            doctorLocation.setConsultationFee(dto.getConsultationFeeAtThisLocation());
            doctorLocationRepository.save(doctorLocation);
        }

        return convertToClinicLocationDTO(updatedClinic, doctorLocation.getConsultationFee());
    }

    public void deleteClinic(Long userId, Long clinicId) {
        logger.info("Deleting clinic ID: {} for doctor user ID: {}", clinicId, userId);
        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        DoctorLocation doctorLocation = doctorLocationRepository.findByDoctorIdAndLocationId(doctor.getId(), clinicId)
                .orElseThrow(() -> new RuntimeException("Clinic not found or unauthorized"));

        // Only remove the doctor-clinic association, not the clinic itself
        doctorLocationRepository.delete(doctorLocation);
    }

    public List<DoctorScheduleDTO> getDoctorSchedules(Long doctorId) {
        logger.info("Fetching schedules for doctor ID: {}", doctorId);
        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorId(doctorId);
        return schedules.stream()
                .map(this::convertToScheduleDTO)
                .collect(Collectors.toList());
    }

    public DoctorScheduleDTO createOrUpdateSchedule(Long userId, DoctorScheduleDTO scheduleDTO) {
        logger.info("Creating or updating schedule for doctor user ID: {}", userId);
        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        ClinicLocation location = clinicLocationRepository.findById(scheduleDTO.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));

        DoctorSchedule schedule = doctorScheduleRepository.findByDoctorIdAndLocationIdAndDayOfWeek(
                doctor.getId(), location.getId(),
                DoctorSchedule.DayOfWeek.valueOf(scheduleDTO.getDayOfWeek().toUpperCase()))
                .orElse(new DoctorSchedule());

        schedule.setDoctor(doctor);
        schedule.setLocation(location);
        schedule.setDayOfWeek(DoctorSchedule.DayOfWeek.valueOf(scheduleDTO.getDayOfWeek().toUpperCase()));
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        schedule.setSlotDuration(scheduleDTO.getSlotDuration());
        schedule.setBreakStartTime(scheduleDTO.getBreakStartTime());
        schedule.setBreakEndTime(scheduleDTO.getBreakEndTime());
        schedule.setActive(scheduleDTO.isActive());

        DoctorSchedule savedSchedule = doctorScheduleRepository.save(schedule);
        return convertToScheduleDTO(savedSchedule);
    }

    public DoctorScheduleDTO updateSchedule(Long userId, Long scheduleId, DoctorScheduleDTO scheduleDTO) {
        logger.info("Updating schedule ID: {} for user ID: {}", scheduleId, userId);
        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!schedule.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("Unauthorized to update this schedule");
        }

        ClinicLocation location = clinicLocationRepository.findById(scheduleDTO.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));

        schedule.setLocation(location);
        schedule.setDayOfWeek(DoctorSchedule.DayOfWeek.valueOf(scheduleDTO.getDayOfWeek().toUpperCase()));
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        schedule.setSlotDuration(scheduleDTO.getSlotDuration());
        schedule.setBreakStartTime(scheduleDTO.getBreakStartTime());
        schedule.setBreakEndTime(scheduleDTO.getBreakEndTime());
        schedule.setActive(scheduleDTO.isActive());

        DoctorSchedule savedSchedule = doctorScheduleRepository.save(schedule);
        return convertToScheduleDTO(savedSchedule);
    }

    public void deleteSchedule(Long userId, Long scheduleId) {
        logger.info("Deleting schedule ID: {} for user ID: {}", scheduleId, userId);
        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!schedule.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("Unauthorized to delete this schedule");
        }

        doctorScheduleRepository.delete(schedule);
    }

    public DoctorScheduleDTO updateScheduleByDate(Long userId, Long locationId, String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        logger.info("Updating schedule for doctor user ID: {}, location: {}, day: {}, times: {} - {}",
                userId, locationId, dayOfWeek, startTime, endTime);
        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        ClinicLocation location = clinicLocationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        DoctorSchedule schedule = doctorScheduleRepository.findByDoctorIdAndLocationIdAndDayOfWeek(
                doctor.getId(), locationId,
                DoctorSchedule.DayOfWeek.valueOf(dayOfWeek.toUpperCase()))
                .orElse(new DoctorSchedule());

        schedule.setDoctor(doctor);
        schedule.setLocation(location);
        schedule.setDayOfWeek(DoctorSchedule.DayOfWeek.valueOf(dayOfWeek.toUpperCase()));
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setActive(true);

        DoctorSchedule savedSchedule = doctorScheduleRepository.save(schedule);

        return convertToScheduleDTO(savedSchedule);
    }

    public void cancelConflictingAppointments(Long doctorId, LocalDate appointmentDate, LocalTime newStartTime, LocalTime newEndTime) {
        logger.info("Cancelling appointments for doctor: {}, date: {}, outside time range: {} - {}",
                doctorId, appointmentDate, newStartTime, newEndTime);

        List<Appointment> appointments = appointmentRepository
                .findDoctorAppointmentsByDateAndStatus(doctorId, appointmentDate, Appointment.AppointmentStatus.SCHEDULED);

        for (Appointment appointment : appointments) {
            LocalTime appointmentTime = appointment.getAppointmentTime();

            if (appointmentTime.isBefore(newStartTime) || appointmentTime.isAfter(newEndTime)) {
                appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
                appointmentRepository.save(appointment);
                logger.info("Cancelled appointment ID: {} at {}", appointment.getId(), appointmentTime);
            }
        }
    }

    public List<String> getAvailableSlots(Long doctorId, Long locationId, LocalDate date) {
        logger.info("Fetching available slots for doc {}, loc {}, date {}", doctorId, locationId, date);
        DoctorSchedule.DayOfWeek day = DoctorSchedule.DayOfWeek.valueOf(date.getDayOfWeek().name());

        DoctorSchedule schedule = doctorScheduleRepository.findActiveSchedule(doctorId, day)
                .orElseThrow(() -> new RuntimeException("No schedule available for this day"));

        List<String> slots = new ArrayList<>();
        LocalTime current = schedule.getStartTime();
        LocalTime end = schedule.getEndTime();
        int duration = schedule.getSlotDuration();

        while (current.isBefore(end)) {
            // Simplified: skip break times
            if (schedule.getBreakStartTime() != null && schedule.getBreakEndTime() != null) {
                if (!current.isBefore(schedule.getBreakStartTime()) && current.isBefore(schedule.getBreakEndTime())) {
                    current = current.plusMinutes(duration);
                    continue;
                }
            }
            slots.add(current.toString());
            current = current.plusMinutes(duration);
        }

        return slots;
    }

    public List<DoctorProfileDTO> getDoctorsBySpecialization(String specialization) {
        logger.info("Fetching doctors by specialty: {}", specialization);
        return doctorRepository.findBySpecialization(specialization).stream()
                .map(this::convertToProfileDTO)
                .collect(Collectors.toList());
    }

    public List<DoctorProfileDTO> getTopRatedDoctors(int limit) {
        logger.info("Fetching top rated doctors, limit: {}", limit);
        Pageable pageable = PageRequest.of(0, limit);
        return doctorRepository.findTopRatedDoctors(BigDecimal.valueOf(4.0), pageable).stream()
                .map(this::convertToProfileDTO)
                .collect(Collectors.toList());
    }

    public List<DoctorProfileDTO> getNearbyDoctors(Double lat, Double lng, Double radius) {
        logger.info("Fetching nearby doctors at {}, {} within radius {}", lat, lng, radius);
        // Simulating proximity by returning all available and verified doctors
        // in a production app, this would use spatial queries (Haversine formula or GIS)
        return doctorRepository.findAllAvailableAndVerified().stream()
                .limit(5)
                .map(this::convertToProfileDTO)
                .collect(Collectors.toList());
    }

    private DoctorProfileDTO convertToProfileDTO(Doctor doctor) {
        DoctorProfileDTO dto = modelMapper.map(doctor, DoctorProfileDTO.class);
        dto.setDoctorId(doctor.getId());
        if (doctor.getUser() != null) {
            dto.setEmail(doctor.getUser().getEmail());
            dto.setPhone(doctor.getUser().getPhone());
        }

        // Fetch and populate clinic locations
        List<DoctorLocation> doctorLocations = doctorLocationRepository.findByDoctorId(doctor.getId());
        List<com.digitalclinic.appointmentsystem.dto.ClinicLocationDTO> clinicDTOs = doctorLocations.stream()
                .map(dl -> convertToClinicLocationDTO(dl.getLocation(), dl.getConsultationFee()))
                .collect(Collectors.toList());
        dto.setClinicLocations(clinicDTOs);

        // Fix profile photo URL - ensure it's a valid URL or use placeholder
        if (dto.getProfilePhoto() == null || dto.getProfilePhoto().isEmpty()) {
            String initials = doctor.getFullName() != null ?
                    doctor.getFullName().split(" ")[0].substring(0, 1) : "D";
            dto.setProfilePhoto("https://ui-avatars.com/api/?name=" + initials + "&background=4F46E5&color=fff");
        }

        return dto;
    }

    private com.digitalclinic.appointmentsystem.dto.ClinicLocationDTO convertToClinicLocationDTO(ClinicLocation clinic, BigDecimal consultationFee) {
        com.digitalclinic.appointmentsystem.dto.ClinicLocationDTO dto = modelMapper.map(clinic, com.digitalclinic.appointmentsystem.dto.ClinicLocationDTO.class);
        dto.setLocationId(clinic.getId());
        dto.setConsultationFeeAtThisLocation(consultationFee);
        return dto;
    }

    private DoctorScheduleDTO convertToScheduleDTO(DoctorSchedule schedule) {
        DoctorScheduleDTO dto = modelMapper.map(schedule, DoctorScheduleDTO.class);
        dto.setScheduleId(schedule.getId());
        dto.setLocationId(schedule.getLocation().getId());
        dto.setClinicName(schedule.getLocation().getClinicName());
        dto.setDayOfWeek(schedule.getDayOfWeek().name());
        return dto;
    }
}
