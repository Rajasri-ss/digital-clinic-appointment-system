package com.digitalclinic.appointmentsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientVitalDTO {
    private Long id;
    private Long patientId;
    private Integer heartRate;
    private String bloodPressure;
    private Double temperature;
    private Double weight;
    private Double height;
    private Integer respiratoryRate;
    private String bloodSugar;
    private Integer oxygenSaturation;
    private String notes;
    private LocalDateTime recordDate;
    private Long recordedById;
}
