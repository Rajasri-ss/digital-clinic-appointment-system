package com.digitalclinic.appointmentsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentUpdateDTO {
    private String status; // SCHEDULED, COMPLETED, CANCELLED, NO_SHOW, IN_PROGRESS
    private String examinationNotes;
    private String doctorNotes;
    private String diagnosis;
}
