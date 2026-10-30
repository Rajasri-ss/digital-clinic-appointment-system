package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.MedicalRecordDTO;
import com.digitalclinic.appointmentsystem.model.MedicalRecord;
import com.digitalclinic.appointmentsystem.model.Patient;
import com.digitalclinic.appointmentsystem.model.User;
import com.digitalclinic.appointmentsystem.repository.MedicalRecordRepository;
import com.digitalclinic.appointmentsystem.repository.PatientRepository;
import com.digitalclinic.appointmentsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads/records}")
    private String uploadDir;

    @Transactional
    public MedicalRecordDTO uploadRecord(Long patientId, MultipartFile file, String recordType,
            String recordName, String description, Long uploadedById) throws IOException {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));

        User uploader = null;
        if (uploadedById != null) {
            uploader = userRepository.findById(uploadedById)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + uploadedById));
        }

        // Save file locally
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        String fileUrl = "/api/records/download/" + fileName;

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .recordType(recordType)
                .recordName(recordName)
                .description(description)
                .fileUrl(fileUrl)
                .uploadedBy(uploader)
                .build();

        MedicalRecord savedRecord = medicalRecordRepository.save(record);
        return mapToDTO(savedRecord);
    }

    public List<MedicalRecordDTO> getRecordsByPatient(Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByUploadDateDesc(patientId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public MedicalRecordDTO getRecordById(Long id) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical Record not found"));
        return mapToDTO(record);
    }

    private MedicalRecordDTO mapToDTO(MedicalRecord record) {
        return MedicalRecordDTO.builder()
                .id(record.getId())
                .patientId(record.getPatient().getId())
                .recordType(record.getRecordType())
                .recordName(record.getRecordName())
                .fileUrl(record.getFileUrl())
                .description(record.getDescription())
                .uploadedById(record.getUploadedBy() != null ? record.getUploadedBy().getId() : null)
                .uploadDate(record.getUploadDate())
                .build();
    }
}
