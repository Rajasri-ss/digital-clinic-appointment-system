package com.digitalclinic.appointmentsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorScheduleUpdateRequest {
    private Long locationId;
    private String dayOfWeek;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
