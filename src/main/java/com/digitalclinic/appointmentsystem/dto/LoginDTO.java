package com.digitalclinic.appointmentsystem.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String identifier; // Email or Phone
    private String password;
}
