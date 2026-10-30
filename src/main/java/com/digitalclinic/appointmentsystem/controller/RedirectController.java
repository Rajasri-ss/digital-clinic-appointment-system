package com.digitalclinic.appointmentsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to handle URL redirects for better user experience
 */
@Controller
public class RedirectController {

    @GetMapping("/login")
    public String redirectLogin() {
        return "redirect:/login.html";
    }

    @GetMapping("/register")
    public String redirectRegister() {
        return "redirect:/register.html";
    }

    @GetMapping("/dashboard")
    public String redirectDashboard() {
        return "redirect:/patient_dashboard.html";
    }

    @GetMapping("/admin")
    public String redirectAdmin() {
        return "redirect:/admin_dashboard.html";
    }

    @GetMapping("/doctor")
    public String redirectDoctor() {
        return "redirect:/doctor_dashboard.html";
    }


    @GetMapping("/register-doctor")
    public String redirectRegisterDoctor() {
        return "redirect:/register_doctor.html";
    }

    @GetMapping("/register-patient")
    public String redirectRegisterPatient() {
        return "redirect:/register_patient.html";
    }

}
