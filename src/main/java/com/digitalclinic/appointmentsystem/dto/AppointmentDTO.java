package com.digitalclinic.appointmentsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {
    private Long appointmentId;
    private String bookingReference;

    private Long patientId;
    private String patientName;
    private String patientPhone;
    private String patientGender;
    private String patientBloodGroup;
    private LocalDate patientDateOfBirth;

    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;

    private Long locationId;
    private String clinicName;
    private String clinicAddress;

    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    private String consultationType;
    private String reasonForVisit;

    private String status;
    private boolean isEmergency;
    private String urgencyLevel;

    private BigDecimal consultationFee;
    private String paymentStatus;

    private String chiefComplaint;
    private String diagnosis;
    private String doctorNotes;
}
