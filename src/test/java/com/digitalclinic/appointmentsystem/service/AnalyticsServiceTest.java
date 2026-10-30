package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.model.Appointment;
import com.digitalclinic.appointmentsystem.model.Payment;
import com.digitalclinic.appointmentsystem.repository.AppointmentRepository;
import com.digitalclinic.appointmentsystem.repository.DoctorRepository;
import com.digitalclinic.appointmentsystem.repository.PatientRepository;
import com.digitalclinic.appointmentsystem.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetDashboardMetrics() {
        when(patientRepository.count()).thenReturn(10L);
        when(doctorRepository.count()).thenReturn(5L);
        when(appointmentRepository.count()).thenReturn(20L);

        Appointment app1 = Appointment.builder().appointmentDate(LocalDate.now()).build();
        when(appointmentRepository.findAll()).thenReturn(List.of(app1));

        Payment p1 = Payment.builder().amount(new BigDecimal("150.0")).status("SUCCESS").build();
        when(paymentRepository.findAll()).thenReturn(List.of(p1));

        Map<String, Object> metrics = analyticsService.getDashboardMetrics();

        assertNotNull(metrics);
        assertEquals(10L, metrics.get("totalPatients"));
        assertEquals(5L, metrics.get("totalDoctors"));
        assertEquals(20L, metrics.get("totalAppointments"));
        assertEquals(1L, metrics.get("todayAppointments"));
        assertEquals(new BigDecimal("150.0"), metrics.get("totalRevenue"));
    }

    @Test
    void testGetAppointmentTrends() {
        Map<String, Object> trends = analyticsService.getAppointmentTrends();
        assertNotNull(trends);
        assertEquals(6, ((List<?>) trends.get("labels")).size());
    }
}
