package com.digitalclinic.appointmentsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAdminDTO {
    private Long id;
    private String fullName;
    private String specialty;
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
    private Boolean isAvailable;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // User information
    private UserBasicDTO user;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBasicDTO {
        private Long id;
        private String email;
        private String phone;
        @JsonProperty("isActive")
        private boolean active;
        private LocalDateTime createdAt;
    }
}
