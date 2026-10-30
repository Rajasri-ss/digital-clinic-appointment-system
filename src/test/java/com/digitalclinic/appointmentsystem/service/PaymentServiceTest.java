package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.PaymentRequestDTO;
import com.digitalclinic.appointmentsystem.dto.PaymentResponseDTO;
import com.digitalclinic.appointmentsystem.model.Appointment;
import com.digitalclinic.appointmentsystem.model.Doctor;
import com.digitalclinic.appointmentsystem.model.Patient;
import com.digitalclinic.appointmentsystem.model.Payment;
import com.digitalclinic.appointmentsystem.model.User;
import com.digitalclinic.appointmentsystem.repository.AppointmentRepository;
import com.digitalclinic.appointmentsystem.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void testProcessPaymentSuccess() {
        Long patientId = 1L;
        Patient patient = new Patient();
        patient.setId(patientId);
        User pUser = new User();
        pUser.setId(10L);
        patient.setUser(pUser);
        patient.setFullName("John Patient");

        Doctor doctor = new Doctor();
        User dUser = new User();
        dUser.setId(20L);
        doctor.setUser(dUser);

        Appointment app = new Appointment();
        app.setId(5L);
        app.setPatient(patient);
        app.setDoctor(doctor);
        app.setStatus(Appointment.AppointmentStatus.SCHEDULED);

        PaymentRequestDTO req = new PaymentRequestDTO();
        req.setAppointmentId(5L);
        req.setProvider("MOCK_STRIPE");
        req.setCardNumberMock("4242424242424242"); // Success trigger
        req.setAmount("100.00");

        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(app));

        Payment mockSaved = Payment.builder()
                .id(99L)
                .appointment(app)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .provider("MOCK_STRIPE")
                .status("SUCCESS")
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(mockSaved);

        PaymentResponseDTO res = paymentService.processPayment(req, patientId);

        assertNotNull(res);
        assertEquals("SUCCESS", res.getStatus());
        assertEquals(Appointment.AppointmentStatus.CONFIRMED, app.getStatus());
        assertEquals(Appointment.PaymentStatus.PAID, app.getPaymentStatus());
        verify(appointmentRepository, times(1)).save(app);
        verify(notificationService, times(2)).sendNotification(anyLong(), anyString(), anyString(), anyString()); // Doctor
                                                                                                                  // and
                                                                                                                  // Patient
    }
}
