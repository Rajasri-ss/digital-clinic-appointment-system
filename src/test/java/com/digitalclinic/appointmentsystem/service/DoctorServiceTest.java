package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.DoctorProfileDTO;
import com.digitalclinic.appointmentsystem.dto.DoctorSearchDTO;
import com.digitalclinic.appointmentsystem.model.Doctor;
import com.digitalclinic.appointmentsystem.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private DoctorService doctorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearchDoctors_WithFilters() {
        // Arrange
        DoctorSearchDTO searchDTO = DoctorSearchDTO.builder()
                .specialization("Cardiologist")
                .page(0)
                .size(10)
                .sortBy("rating")
                .build();

        Doctor doc1 = new Doctor();
        doc1.setId(1L);
        doc1.setSpecialization("Cardiologist");

        DoctorProfileDTO doc1DTO = new DoctorProfileDTO();
        doc1DTO.setDoctorId(1L);
        doc1DTO.setSpecialization("Cardiologist");

        Page<Doctor> mockedPage = new PageImpl<>(Arrays.asList(doc1));

        when(doctorRepository.searchDoctors(any(), any(), any(), any(PageRequest.class)))
                .thenReturn(mockedPage);
        when(modelMapper.map(any(Doctor.class), eq(DoctorProfileDTO.class)))
                .thenReturn(doc1DTO);

        // Act
        Page<DoctorProfileDTO> result = doctorService.searchDoctors(searchDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getSize());
        assertEquals("Cardiologist", result.getContent().get(0).getSpecialization());
        verify(doctorRepository, times(1)).searchDoctors(any(), any(), any(), any());
    }

    @Test
    void testGetDoctorProfile_Success() {
        // Arrange
        Long doctorId = 1L;
        Doctor doc = new Doctor();
        doc.setId(doctorId);
        doc.setFullName("Dr. Smith");

        DoctorProfileDTO dto = new DoctorProfileDTO();
        dto.setDoctorId(doctorId);
        dto.setFullName("Dr. Smith");

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doc));
        when(modelMapper.map(doc, DoctorProfileDTO.class)).thenReturn(dto);

        // Act
        DoctorProfileDTO result = doctorService.getDoctorProfile(doctorId);

        // Assert
        assertNotNull(result);
        assertEquals("Dr. Smith", result.getFullName());
    }

    @Test
    void testGetDoctorProfile_NotFound() {
        // Arrange
        Long doctorId = 99L;
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorService.getDoctorProfile(doctorId);
        });
        assertEquals("Doctor not found", exception.getMessage());
    }

    @Test
    void testUpdateDoctorProfile_Success() {
        // Arrange
        Long userId = 100L;
        DoctorProfileDTO updateDTO = new DoctorProfileDTO();
        updateDTO.setFullName("Dr. Updated");
        updateDTO.setConsultationFee(new BigDecimal("150.00"));

        Doctor existingDoc = new Doctor();
        existingDoc.setId(1L);
        existingDoc.setFullName("Dr. Expected");

        when(doctorRepository.findByUser_Id(userId)).thenReturn(Optional.of(existingDoc));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(existingDoc);
        when(modelMapper.map(any(Doctor.class), eq(DoctorProfileDTO.class))).thenReturn(updateDTO);

        // Act
        DoctorProfileDTO result = doctorService.updateDoctorProfile(userId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Dr. Updated", result.getFullName());
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }
}
