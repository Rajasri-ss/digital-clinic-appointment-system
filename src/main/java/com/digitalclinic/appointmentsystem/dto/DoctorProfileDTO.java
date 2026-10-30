package com.digitalclinic.appointmentsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileDTO {
    private Long doctorId;
    private String fullName;
    private String email;
    private String phone;
    private String specialization;

    private String qualifications;
    private String licenseNumber;
    private Integer experienceYears;

    private String about;
    private String profilePhoto;
    private String languagesSpoken;

    private BigDecimal consultationFee;
    private BigDecimal averageRating;
    private Integer totalReviews;

    private List<ClinicLocationDTO> clinicLocations;
    private List<DoctorScheduleDTO> schedules;

    private Boolean isAvailable;
    private Boolean isVerified;
}
