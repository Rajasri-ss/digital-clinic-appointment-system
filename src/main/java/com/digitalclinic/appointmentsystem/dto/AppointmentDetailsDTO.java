package com.digitalclinic.appointmentsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDetailsDTO {
    private Long appointmentId;
    private LocalTime appointmentTime;
    private String status;
    private PatientShortDTO patient;
    private String symptoms;
}
