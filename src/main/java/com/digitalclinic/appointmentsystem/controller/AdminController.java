package com.digitalclinic.appointmentsystem.controller;

import com.digitalclinic.appointmentsystem.dto.AdminUserDTO;
import com.digitalclinic.appointmentsystem.dto.DoctorAdminDTO;
import com.digitalclinic.appointmentsystem.model.Patient;
import com.digitalclinic.appointmentsystem.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<AdminUserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.debug("GET /api/admin/users - page: {}, size: {}", page, size);
        return ResponseEntity.ok(adminUserService.getAllUsers(page, size));
    }

    @GetMapping("/patients")
    public ResponseEntity<Page<Patient>> getPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.debug("GET /api/admin/users/patients - page: {}, size: {}", page, size);
        return ResponseEntity.ok(adminUserService.getAllPatients(page, size));
    }

    @GetMapping("/doctors")
    public ResponseEntity<Page<DoctorAdminDTO>> getDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.debug("GET /api/admin/users/doctors - page: {}, size: {}", page, size);
        return ResponseEntity.ok(adminUserService.getAllDoctors(page, size));
    }

    @GetMapping("/admins")
    public ResponseEntity<Page<AdminUserDTO>> getAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.debug("GET /api/admin/users/admins - page: {}, size: {}", page, size);
        return ResponseEntity.ok(adminUserService.getAllAdmins(page, size));
    }

    // Get single doctor by ID
    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<DoctorAdminDTO> getDoctorById(@PathVariable Long doctorId) {
        logger.debug("GET /api/admin/users/doctors/{} - fetching doctor details", doctorId);
        try {
            DoctorAdminDTO doctor = adminUserService.getDoctorById(doctorId);
            return ResponseEntity.ok(doctor);
        } catch (RuntimeException e) {
            logger.error("Error fetching doctor {}: {}", doctorId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Get single patient by ID
    @GetMapping("/patients/{patientId}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long patientId) {
        logger.debug("GET /api/admin/users/patients/{} - fetching patient details", patientId);
        try {
            Patient patient = adminUserService.getPatientById(patientId);
            return ResponseEntity.ok(patient);
        } catch (RuntimeException e) {
            logger.error("Error fetching patient {}: {}", patientId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Get single user by ID
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable Long userId) {
        logger.debug("GET /api/admin/users/{} - fetching user details", userId);
        try {
            AdminUserDTO user = adminUserService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            logger.error("Error fetching user {}: {}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{userId}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId) {
        logger.debug("PATCH /api/admin/users/{}/toggle-status", userId);
        try {
            adminUserService.toggleUserStatus(userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            logger.error("Error toggling user status for userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error toggling user status for userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500).body(java.util.Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @PatchMapping("/doctors/{doctorId}/approve")
    public ResponseEntity<?> approveDoctor(@PathVariable Long doctorId) {
        logger.debug("PATCH /api/admin/users/doctors/{}/approve", doctorId);
        try {
            adminUserService.approveDoctor(doctorId);
            return ResponseEntity.ok().body(java.util.Map.of("message", "Doctor approved successfully"));
        } catch (RuntimeException e) {
            logger.error("Error approving doctor {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error approving doctor {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.status(500).body(java.util.Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @PatchMapping("/doctors/{doctorId}/reject")
    public ResponseEntity<?> rejectDoctor(@PathVariable Long doctorId) {
        logger.debug("PATCH /api/admin/users/doctors/{}/reject", doctorId);
        try {
            adminUserService.rejectDoctor(doctorId);
            return ResponseEntity.ok().body(java.util.Map.of("message", "Doctor rejected successfully"));
        } catch (RuntimeException e) {
            logger.error("Error rejecting doctor {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error rejecting doctor {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.status(500).body(java.util.Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}
