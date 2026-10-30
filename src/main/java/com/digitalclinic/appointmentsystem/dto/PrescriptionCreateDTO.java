package com.digitalclinic.appointmentsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PrescriptionCreateDTO {

    private Long patientId;

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;

    private String instructions;
    private Boolean followUpRequired = false;
    private LocalDate followUpDate;

    private List<MedicationDTO> medications;
}
