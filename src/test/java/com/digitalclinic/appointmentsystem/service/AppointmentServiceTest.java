package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.AppointmentDTO;
import com.digitalclinic.appointmentsystem.dto.EmergencyBookingDTO;
import com.digitalclinic.appointmentsystem.model.*;
import com.digitalclinic.appointmentsystem.repository.AppointmentRepository;
import com.digitalclinic.appointmentsystem.repository.ClinicLocationRepository;
import com.digitalclinic.appointmentsystem.repository.DoctorRepository;
import com.digitalclinic.appointmentsystem.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private ClinicLocationRepository locationRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setFullName("John Doe");

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setFullName("Dr. Smith");
    }

    @Test
    void testBookEmergencyAppointment_Success() {
        EmergencyBookingDTO dto = new EmergencyBookingDTO();
        dto.setDoctorId(1L);
        dto.setAppointmentDate(LocalDate.now());
        dto.setAppointmentTime(LocalTime.now());
        dto.setChiefComplaint("Chest Pain");
        dto.setLocationId(10L);

        ClinicLocation clinic = new ClinicLocation();
        clinic.setId(10L);

        when(patientRepository.findByUser_Id(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(clinic));

        Appointment savedAppointment = Appointment.builder()
                .id(100L)
                .patient(patient)
                .doctor(doctor)
                .location(clinic)
                .appointmentDate(LocalDate.now())
                .appointmentTime(LocalTime.now())
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .urgencyLevel(Appointment.UrgencyLevel.CRITICAL)
                .build();

        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        AppointmentDTO mockDto = new AppointmentDTO();
        mockDto.setStatus(Appointment.AppointmentStatus.SCHEDULED.name());
        mockDto.setUrgencyLevel("CRITICAL");
        when(modelMapper.map(any(), any())).thenReturn(mockDto);

        AppointmentDTO response = appointmentService.bookEmergencyAppointment(1L, dto);

        assertNotNull(response);
        assertEquals(Appointment.AppointmentStatus.SCHEDULED.name(), response.getStatus());
        assertEquals("CRITICAL", response.getUrgencyLevel());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }
}
