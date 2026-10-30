package com.digitalclinic.appointmentsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientShortDTO {
    private Long patientId;
    private String name;
    private Integer age;
    private String gender;
    private String bloodGroup;
}
