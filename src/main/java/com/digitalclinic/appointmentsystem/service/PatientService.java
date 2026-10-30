package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.PatientProfileDTO;
import com.digitalclinic.appointmentsystem.dto.PatientVitalDTO;
import com.digitalclinic.appointmentsystem.model.Patient;
import com.digitalclinic.appointmentsystem.repository.PatientRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PatientVitalService patientVitalService;

    @Autowired
    private com.digitalclinic.appointmentsystem.repository.AppointmentRepository appointmentRepository;

    public PatientProfileDTO getPatientProfile(Long userId) {
        logger.info("Fetching patient profile for user ID: {}", userId);
        Patient patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found for user ID: " + userId));
        return convertToDTO(patient);
    }

    public PatientProfileDTO updatePatientProfile(Long userId, PatientProfileDTO dto) {
        logger.info("Updating patient profile for user ID: {}", userId);
        Patient patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        patient.setFullName(dto.getFullName());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setBloodGroup(dto.getBloodGroup());
        patient.setAddress(dto.getAddress());
        patient.setCity(dto.getCity());
        patient.setState(dto.getState());
        patient.setPincode(dto.getPincode());
        patient.setEmergencyContact(dto.getEmergencyContact());
        patient.setEmergencyContactName(dto.getEmergencyContactName());

        Patient updatedPatient = patientRepository.save(patient);
        return convertToDTO(updatedPatient);
    }

    public PatientProfileDTO getMedicalHistory(Long userId) {
        logger.info("Fetching medical history for user ID: {}", userId);
        Patient patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));
        return convertToDTO(patient);
    }

    public void updateMedicalHistory(Long userId, String allergies, String chronicConditions, String medicalHistory) {
        logger.info("Updating medical history for user ID: {}", userId);
        Patient patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        patient.setAllergies(allergies);
        patient.setChronicConditions(chronicConditions);
        patient.setMedicalHistory(medicalHistory);

        patientRepository.save(patient);
    }

    public Map<String, Object> getPatientStats(Long userId) {
        logger.info("Fetching stats for user ID: {}", userId);
        Patient patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAppointments", appointmentRepository.countByPatientId(patient.getId()));
        stats.put("upcomingCount", appointmentRepository.countUpcomingAppointmentsByPatientId(patient.getId()));
        stats.put("completedCount", appointmentRepository.countCompletedAppointmentsByPatientId(patient.getId()));
        stats.put("cancelledCount", appointmentRepository.countCancelledAppointmentsByPatientId(patient.getId()));
        return stats;
    }

    public Map<String, Object> getPatientDashboard(Long userId) {
        logger.info("Fetching dashboard data for patient user ID: {}", userId);
        Patient patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        Map<String, Object> dashboard = new HashMap<>();
        
        // Patient profile info
        dashboard.put("patientName", patient.getFullName());
        dashboard.put("bloodGroup", patient.getBloodGroup());
        dashboard.put("allergies", patient.getAllergies());
        dashboard.put("emergencyContact", patient.getEmergencyContact());
        dashboard.put("emergencyContactName", patient.getEmergencyContactName());
        
        // Appointment stats
        dashboard.put("upcomingAppointments", appointmentRepository.countUpcomingAppointmentsByPatientId(patient.getId()));
        dashboard.put("completedAppointments", appointmentRepository.countCompletedAppointmentsByPatientId(patient.getId()));
        dashboard.put("totalAppointments", appointmentRepository.countByPatientId(patient.getId()));
        
        // Recent appointments (limit to 5)
        List<Map<String, Object>> recentAppointments = appointmentRepository
                .findUpcomingAppointmentsByPatientId(patient.getId())
                .stream()
                .limit(5)
                .map(apt -> {
                    Map<String, Object> aptMap = new HashMap<>();
                    aptMap.put("appointmentId", apt.getId());
                    aptMap.put("doctorName", apt.getDoctor().getFullName());
                    aptMap.put("specialization", apt.getDoctor().getSpecialization());
                    aptMap.put("appointmentDate", apt.getAppointmentDate().toString());
                    aptMap.put("appointmentTime", apt.getAppointmentTime().toString());
                    aptMap.put("status", apt.getStatus().name());
                    if (apt.getLocation() != null) {
                        aptMap.put("clinicName", apt.getLocation().getClinicName());
                    }
                    return aptMap;
                })
                .collect(Collectors.toList());
        
        dashboard.put("recentAppointments", recentAppointments);
        
        return dashboard;
    }

    public List<PatientProfileDTO> searchPatients(String searchTerm) {
        logger.info("Searching patients with term: {}", searchTerm);
        List<Patient> patients = patientRepository.findByFullNameContainingIgnoreCase(searchTerm);
        return patients.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PatientVitalDTO> getPatientVitals(Long userId) {
        logger.info("Fetching vitals for user ID: {}", userId);
        Patient patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));
        return patientVitalService.getPatientVitals(patient.getId());
    }

    private PatientProfileDTO convertToDTO(Patient patient) {
        PatientProfileDTO dto = modelMapper.map(patient, PatientProfileDTO.class);
        dto.setPatientId(patient.getId());
        if (patient.getUser() != null) {
            dto.setEmail(patient.getUser().getEmail());
        }
        return dto;
    }
}
