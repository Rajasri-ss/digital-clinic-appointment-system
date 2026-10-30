package com.digitalclinic.appointmentsystem.controller;

import com.digitalclinic.appointmentsystem.dto.PatientProfileDTO;
import com.digitalclinic.appointmentsystem.dto.PatientVitalDTO;
import com.digitalclinic.appointmentsystem.security.UserDetailsImpl;
import com.digitalclinic.appointmentsystem.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientProfileDTO> getMyProfile(Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(patientService.getPatientProfile(userDetails.getId()));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientProfileDTO> updateProfile(
            @RequestBody @Valid PatientProfileDTO dto, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(patientService.updatePatientProfile(userDetails.getId(), dto));
    }

    @GetMapping("/medical-history")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientProfileDTO> getMedicalHistory(Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(patientService.getMedicalHistory(userDetails.getId()));
    }

    @PutMapping("/medical-history")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<String> updateMedicalHistory(
            @RequestBody Map<String, String> medicalData, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        patientService.updateMedicalHistory(
                userDetails.getId(),
                medicalData.get("allergies"),
                medicalData.get("chronicConditions"),
                medicalData.get("medicalHistory"));
        return ResponseEntity.ok("Medical history updated successfully");
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> getMyStats(Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(patientService.getPatientStats(userDetails.getId()));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> getDashboard(Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(patientService.getPatientDashboard(userDetails.getId()));
    }

    @GetMapping("/vitals")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<PatientVitalDTO>> getMyVitals(Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(patientService.getPatientVitals(userDetails.getId()));
    }
}
