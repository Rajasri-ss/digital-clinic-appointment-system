package com.digitalclinic.appointmentsystem.dto;

import com.digitalclinic.appointmentsystem.model.Role;
import lombok.Data;

@Data
public class UserRegistrationDTO {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String dob;
    private String gender;
    private String bloodGroup;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private Role role; // PATIENT or DOCTOR
}
