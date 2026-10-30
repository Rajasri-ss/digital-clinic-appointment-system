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
public class MedicalRecordDTO {
    private Long id;
    private Long patientId;
    private String recordType;
    private String recordName;
    private String fileUrl;
    private String description;
    private Long uploadedById;
    private LocalDateTime uploadDate;
}
