package com.digitalclinic.appointmentsystem.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicationResponseDTO {
    private Long medicationId;
    private String medicineName;
    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;
}
