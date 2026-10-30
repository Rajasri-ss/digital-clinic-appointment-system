package com.digitalclinic.appointmentsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentUpdateRequestDTO {
    
    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;
    
    private String doctorNotes;
    
    @NotBlank(message = "Status is required")
    private String status;
}
