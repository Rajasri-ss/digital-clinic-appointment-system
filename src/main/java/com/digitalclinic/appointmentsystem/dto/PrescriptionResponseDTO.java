package com.digitalclinic.appointmentsystem.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PrescriptionResponseDTO {
    private Long prescriptionId;
    private Long appointmentId;
    private String doctorName;
    private String patientName;
    private LocalDate prescriptionDate;
    private String diagnosis;
    private String instructions;
    private Boolean followUpRequired;
    private LocalDate followUpDate;
    private String pdfUrl;

    private List<MedicationResponseDTO> medications;
}
