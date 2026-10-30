package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.PrescriptionDTO;
import com.digitalclinic.appointmentsystem.model.Doctor;
import com.digitalclinic.appointmentsystem.model.Patient;
import com.digitalclinic.appointmentsystem.model.Prescription;
import com.digitalclinic.appointmentsystem.repository.AppointmentRepository;
import com.digitalclinic.appointmentsystem.repository.DoctorRepository;
import com.digitalclinic.appointmentsystem.repository.PatientRepository;
import com.digitalclinic.appointmentsystem.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(prescriptionService, "prescriptionUploadDir", "target/test-uploads/prescriptions");
    }

    @Test
    void testCreatePrescription() {
        PrescriptionDTO reqDto = PrescriptionDTO.builder()
                .patientId(1L)
                .doctorId(2L)
                .diagnosis("Flu")
                .medications(new ArrayList<>())
                .build();

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFullName("John Doe");

        Doctor doctor = new Doctor();
        doctor.setId(2L);
        doctor.setFullName("Jane Smith");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));

        Prescription saved = Prescription.builder()
                .id(10L)
                .patient(patient)
                .doctor(doctor)
                .diagnosis("Flu")
                .prescriptionDate(LocalDateTime.now())
                .pdfUrl("/api/prescriptions/download/test.pdf")
                .build();

        // The service calls save twice (once before pdf, once after).
        // We can just stub it to return 'saved' on any call.
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(saved);

        PrescriptionDTO resDto = prescriptionService.createPrescription(reqDto);

        assertNotNull(resDto);
        assertEquals(10L, resDto.getId());
        assertEquals("Flu", resDto.getDiagnosis());
        assertTrue(resDto.getPdfUrl().startsWith("/api/prescriptions/download/prescription_10_"));
        verify(prescriptionRepository, atLeast(1)).save(any(Prescription.class));
    }
}
