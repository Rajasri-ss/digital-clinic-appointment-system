package com.digitalclinic.appointmentsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class EmergencyBookingDTO {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Location ID is required")
    private Long locationId;

    @NotNull(message = "Appointment Date is required")
    private LocalDate appointmentDate;

    @NotNull(message = "Appointment Time is required")
    private LocalTime appointmentTime;

    private String consultationType; // IN_PERSON, VIDEO, PHONE

    @NotBlank(message = "Reason for visit is required for emergency")
    private String reasonForVisit;

    @Builder.Default
    private boolean isEmergency = true;

    @NotBlank(message = "Urgency Level is required (CRITICAL, HIGH, MEDIUM)")
    private String urgencyLevel;

    @NotBlank(message = "Chief complaint is required for emergency triage")
    private String chiefComplaint;

    private String symptoms;
}
