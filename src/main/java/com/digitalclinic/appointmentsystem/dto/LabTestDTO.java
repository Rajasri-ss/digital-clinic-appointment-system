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
public class LabTestDTO {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private String testName;
    private String status;
    private String result;
    private String fileUrl;
    private String remarks;
    private LocalDateTime createdDate;
    private LocalDateTime completedDate;
}
