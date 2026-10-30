package com.digitalclinic.appointmentsystem.controller;

import com.digitalclinic.appointmentsystem.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(analyticsService.getDashboardMetrics());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
        return ResponseEntity.ok(analyticsService.getDashboardMetrics());
    }


    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getTrends() {
        return ResponseEntity.ok(analyticsService.getAppointmentTrends());
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        Map<String, Object> metrics = analyticsService.getDashboardMetrics();
        String csv = "Metric,Value\n" +
                "Total Patients," + metrics.get("totalPatients") + "\n" +
                "Total Doctors," + metrics.get("totalDoctors") + "\n" +
                "Total Appointments," + metrics.get("totalAppointments") + "\n" +
                "Total Revenue," + metrics.get("totalRevenue");

        byte[] data = csv.getBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=analytics_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }
}
