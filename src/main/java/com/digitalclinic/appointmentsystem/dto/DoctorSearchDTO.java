package com.digitalclinic.appointmentsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSearchDTO {
    private String specialization;
    private String city;
    private Integer minExperience;
    private BigDecimal maxFee;
    private BigDecimal minRating;
    private String availability;

    @Builder.Default
    private String sortBy = "rating";

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;
}
