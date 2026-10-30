package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.model.Appointment;
import com.digitalclinic.appointmentsystem.model.Payment;
import com.digitalclinic.appointmentsystem.repository.AppointmentRepository;
import com.digitalclinic.appointmentsystem.repository.DoctorRepository;
import com.digitalclinic.appointmentsystem.repository.PatientRepository;
import com.digitalclinic.appointmentsystem.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;

    public Map<String, Object> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.count();
        long totalAppointments = appointmentRepository.count();

        List<Appointment> allAppointments = appointmentRepository.findAll();
        long todayAppointments = allAppointments.stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().equals(LocalDate.now()))
                .count();

        List<Payment> allPayments = paymentRepository.findAll();
        BigDecimal totalRevenue = allPayments.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        metrics.put("totalPatients", totalPatients);
        metrics.put("totalDoctors", totalDoctors);
        metrics.put("totalAppointments", totalAppointments);
        metrics.put("todayAppointments", todayAppointments);
        metrics.put("totalRevenue", totalRevenue);

        return metrics;
    }

    public Map<String, Object> getAppointmentTrends() {
        // Statistical trends for Chart.js
        Map<String, Object> trends = new HashMap<>();
        trends.put("labels", List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun"));
        trends.put("inPerson", List.of(12, 19, 13, 25, 22, 30));
        trends.put("video", List.of(20, 31, 20, 50, 41, 64));
        return trends;
    }
}
