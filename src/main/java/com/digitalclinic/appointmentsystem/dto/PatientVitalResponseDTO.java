package com.digitalclinic.appointmentsystem.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PatientVitalResponseDTO {
    private Long vitalId;
    private Long patientId;
    private Long appointmentId;
    private String bloodPressure;
    private Integer heartRate;
    private BigDecimal temperature;
    private BigDecimal weight;
    private BigDecimal height;
    private BigDecimal bmi;
    private Integer oxygenSaturation;
    private LocalDateTime recordedAt;
}
