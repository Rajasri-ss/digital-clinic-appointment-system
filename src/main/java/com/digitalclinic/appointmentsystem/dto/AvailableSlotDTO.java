package com.digitalclinic.appointmentsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotDTO {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    
    @JsonProperty("isEmergencySlot")
    private boolean isEmergencySlot;
    
    @JsonProperty("isAvailable")
    private boolean isAvailable;
}
