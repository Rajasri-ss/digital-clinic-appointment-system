package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.MedicalRecordDTO;
import com.digitalclinic.appointmentsystem.model.MedicalRecord;
import com.digitalclinic.appointmentsystem.model.Patient;
import com.digitalclinic.appointmentsystem.model.User;
import com.digitalclinic.appointmentsystem.repository.MedicalRecordRepository;
import com.digitalclinic.appointmentsystem.repository.PatientRepository;
import com.digitalclinic.appointmentsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(medicalRecordService, "uploadDir", "target/test-uploads/records");
    }

    @Test
    void testUploadRecordSuccess() throws IOException {
        Long patientId = 1L;
        Long uploadedById = 2L;
        Patient patient = new Patient();
        patient.setId(patientId);

        User user = new User();
        user.setId(uploadedById);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "dummy content".getBytes());

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(userRepository.findById(uploadedById)).thenReturn(Optional.of(user));

        MedicalRecord savedRecord = MedicalRecord.builder()
                .id(10L)
                .patient(patient)
                .recordType("LAB_REPORT")
                .recordName("Blood Test")
                .fileUrl("/api/records/download/test.pdf")
                .uploadedBy(user)
                .build();

        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(savedRecord);

        MedicalRecordDTO result = medicalRecordService.uploadRecord(
                patientId, file, "LAB_REPORT", "Blood Test", "Routine", uploadedById);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(patientId, result.getPatientId());
        assertEquals("LAB_REPORT", result.getRecordType());
        verify(medicalRecordRepository, times(1)).save(any(MedicalRecord.class));
    }

    @Test
    void testGetRecordsByPatient() {
        Long patientId = 1L;
        Patient patient = new Patient();
        patient.setId(patientId);

        MedicalRecord rec1 = MedicalRecord.builder()
                .id(1L)
                .patient(patient)
                .recordType("SCAN")
                .build();

        MedicalRecord rec2 = MedicalRecord.builder()
                .id(2L)
                .patient(patient)
                .recordType("PRESCRIPTION")
                .build();

        when(medicalRecordRepository.findByPatientIdOrderByUploadDateDesc(patientId))
                .thenReturn(Arrays.asList(rec1, rec2));

        List<MedicalRecordDTO> dtoList = medicalRecordService.getRecordsByPatient(patientId);

        assertNotNull(dtoList);
        assertEquals(2, dtoList.size());
        assertEquals(1L, dtoList.get(0).getId());
        assertEquals(2L, dtoList.get(1).getId());
    }
}
