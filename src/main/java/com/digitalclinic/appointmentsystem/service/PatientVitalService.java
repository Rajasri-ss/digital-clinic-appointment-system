package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.PatientVitalDTO;
import com.digitalclinic.appointmentsystem.model.Patient;
import com.digitalclinic.appointmentsystem.model.PatientVital;
import com.digitalclinic.appointmentsystem.model.User;
import com.digitalclinic.appointmentsystem.repository.PatientRepository;
import com.digitalclinic.appointmentsystem.repository.PatientVitalRepository;
import com.digitalclinic.appointmentsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PatientVitalService {

    @Autowired
    private PatientVitalRepository vitalRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    public PatientVitalDTO addVitalSign(Long doctorId, PatientVitalDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor user not found"));

        PatientVital vital = PatientVital.builder()
                .patient(patient)
                .heartRate(dto.getHeartRate())
                .bloodPressure(dto.getBloodPressure())
                .temperature(dto.getTemperature())
                .weight(dto.getWeight())
                .height(dto.getHeight())
                .respiratoryRate(dto.getRespiratoryRate())
                .bloodSugar(dto.getBloodSugar())
                .oxygenSaturation(dto.getOxygenSaturation())
                .notes(dto.getNotes())
                .recordedBy(doctor)
                .build();

        PatientVital savedVital = vitalRepository.save(vital);
        return mapToDTO(savedVital);
    }

    public List<PatientVitalDTO> getPatientVitals(Long patientId) {
        return vitalRepository.findByPatientIdOrderByRecordDateDesc(patientId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private PatientVitalDTO mapToDTO(PatientVital vital) {
        return PatientVitalDTO.builder()
                .id(vital.getId())
                .patientId(vital.getPatient().getId())
                .heartRate(vital.getHeartRate())
                .bloodPressure(vital.getBloodPressure())
                .temperature(vital.getTemperature())
                .weight(vital.getWeight())
                .height(vital.getHeight())
                .respiratoryRate(vital.getRespiratoryRate())
                .bloodSugar(vital.getBloodSugar())
                .oxygenSaturation(vital.getOxygenSaturation())
                .notes(vital.getNotes())
                .recordDate(vital.getRecordDate())
                .recordedById(vital.getRecordedBy() != null ? vital.getRecordedBy().getId() : null)
                .build();
    }
}
