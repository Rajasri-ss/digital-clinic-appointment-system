package com.digitalclinic.appointmentsystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HospitalDTO {
    private Long id;
    private String hospitalName;
    private String hospitalAddress;
    private String city;
    private String state;
    private String pincode;
    private String phone;
    private String email;
    private Integer totalBeds;
    private Boolean emergencyServices;
    private Boolean ambulanceServices;
    private Boolean isActive;
    private String description;
    private String facilities;
    private String imageUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer doctorCount;
}