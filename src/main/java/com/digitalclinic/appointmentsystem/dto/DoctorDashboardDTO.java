package com.digitalclinic.appointmentsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDashboardDTO {
    private List<AppointmentDTO> todayAppointments;
    private DashboardMetricsDTO metrics;
    private List<AppointmentDTO> emergencyAppointments;
}
