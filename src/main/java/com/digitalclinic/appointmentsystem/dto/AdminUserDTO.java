package com.digitalclinic.appointmentsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDTO {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    @JsonProperty("isActive")
    private boolean active;

    private String role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
