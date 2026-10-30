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
public class ClinicLocationDTO {
    private Long locationId;
    private String clinicName;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String phone;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String facilities;
    private String operatingHours;
    private BigDecimal consultationFeeAtThisLocation;
}
