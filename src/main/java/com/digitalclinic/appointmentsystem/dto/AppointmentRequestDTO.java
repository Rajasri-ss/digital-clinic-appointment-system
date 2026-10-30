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
public class AppointmentRequestDTO {
    private Long doctorId;
    private Long locationId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String consultationType; // IN_PERSON, VIDEO
    private String reasonForVisit;
    private boolean isEmergency;
    private String urgencyLevel; // LOW, MEDIUM, HIGH, CRITICAL
}
