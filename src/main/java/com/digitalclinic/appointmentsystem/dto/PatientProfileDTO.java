package com.digitalclinic.appointmentsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfileDTO {
    private Long patientId;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;

    private String address;
    private String city;
    private String state;
    private String pincode;

    private String emergencyContact;
    private String emergencyContactName;

    private String allergies;
    private String chronicConditions;
    private String medicalHistory;

    private String profilePhoto;
    private Integer totalAppointments;
    private Integer upcomingAppointments;
}
