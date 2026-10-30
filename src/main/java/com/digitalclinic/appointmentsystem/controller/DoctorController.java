package com.digitalclinic.appointmentsystem.controller;

import com.digitalclinic.appointmentsystem.dto.*;
import com.digitalclinic.appointmentsystem.security.UserDetailsImpl;
import com.digitalclinic.appointmentsystem.service.AppointmentService;
import com.digitalclinic.appointmentsystem.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    // PUBLIC ENDPOINTS

    @GetMapping("/search")
    public ResponseEntity<Page<DoctorProfileDTO>> searchDoctors(@ModelAttribute DoctorSearchDTO searchDTO) {
        return ResponseEntity.ok(doctorService.searchDoctors(searchDTO));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<DoctorProfileDTO>> getNearbyDoctors(
            @RequestParam("lat") Double lat,
            @RequestParam("lng") Double lng,
            @RequestParam(value = "radius", defaultValue = "10") Double radius) {
        return ResponseEntity.ok(doctorService.getNearbyDoctors(lat, lng, radius));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorProfileDTO> getDoctorProfile(@PathVariable("id") Long id) {
        return ResponseEntity.ok(doctorService.getDoctorProfile(id));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<List<String>> getAvailableSlots(
            @PathVariable Long id,
            @RequestParam Long locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(doctorService.getAvailableSlots(id, locationId, date));
    }

    @GetMapping("/specialization/{spec}")
    public ResponseEntity<List<DoctorProfileDTO>> getBySpecialization(@PathVariable String spec) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(spec));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<DoctorProfileDTO>> getTopRatedDoctors(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(doctorService.getTopRatedDoctors(limit));
    }

    // DOCTOR-ONLY ENDPOINTS

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileDTO> getMyProfile(Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(doctorService.getMyProfile(userDetails.getId()));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileDTO> updateProfile(
            @RequestBody @Valid DoctorProfileDTO dto, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(doctorService.updateDoctorProfile(userDetails.getId(), dto));
    }

    @GetMapping("/schedules")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<DoctorScheduleDTO>> getMySchedules(Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        DoctorProfileDTO doctorProfile = doctorService.getMyProfile(userDetails.getId());
        return ResponseEntity.ok(doctorService.getDoctorSchedules(doctorProfile.getDoctorId()));
    }

    @PostMapping("/schedules")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorScheduleDTO> createSchedule(
            @RequestBody @Valid DoctorScheduleDTO dto, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(doctorService.createOrUpdateSchedule(userDetails.getId(), dto));
    }

    @PutMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorScheduleDTO> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody @Valid DoctorScheduleDTO dto, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(doctorService.updateSchedule(userDetails.getId(), scheduleId, dto));
    }

    @PutMapping("/schedules/update")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorScheduleUpdateResponse> updateScheduleByDate(
            @RequestBody @Valid DoctorScheduleUpdateRequest request, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        DoctorScheduleDTO scheduleDTO = doctorService.updateScheduleByDate(
                userDetails.getId(),
                request.getLocationId(),
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime()
        );

        doctorService.cancelConflictingAppointments(
                userDetails.getId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        DoctorScheduleUpdateResponse response = new DoctorScheduleUpdateResponse();
        response.setSchedule(scheduleDTO);
        response.setMessage("Schedule updated successfully. Conflicting appointments cancelled.");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> deleteSchedule(
            @PathVariable Long scheduleId, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        doctorService.deleteSchedule(userDetails.getId(), scheduleId);
        return ResponseEntity.ok("Schedule deleted successfully");
    }

    // Legacy endpoints for backward compatibility
    @PostMapping("/schedule")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorScheduleDTO> createOrUpdateSchedule(
            @RequestBody @Valid DoctorScheduleDTO dto, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(doctorService.createOrUpdateSchedule(userDetails.getId(), dto));
    }

    @DeleteMapping("/schedule/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> deleteScheduleLegacy(
            @PathVariable Long id, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        doctorService.deleteSchedule(userDetails.getId(), id);
        return ResponseEntity.ok("Schedule deleted successfully");
    }

    // CLINIC LOCATION MANAGEMENT

    @GetMapping("/me/clinics")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<ClinicLocationDTO>> getMyClinics(Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(doctorService.getDoctorClinics(userDetails.getId()));
    }

    @GetMapping("/{doctorId}/clinics")
    public ResponseEntity<List<ClinicLocationDTO>> getDoctorClinics(@PathVariable Long doctorId) {
        return ResponseEntity.ok(doctorService.getDoctorClinicsByDoctorId(doctorId));
    }

    @PostMapping("/clinics")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ClinicLocationDTO> addClinic(
            @RequestBody @Valid ClinicLocationDTO dto, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(doctorService.addClinic(userDetails.getId(), dto));
    }

    @PutMapping("/clinics/{clinicId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ClinicLocationDTO> updateClinic(
            @PathVariable Long clinicId,
            @RequestBody @Valid ClinicLocationDTO dto,
            Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(doctorService.updateClinic(userDetails.getId(), clinicId, dto));
    }

    @DeleteMapping("/clinics/{clinicId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> deleteClinic(
            @PathVariable Long clinicId, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        doctorService.deleteClinic(userDetails.getId(), clinicId);
        return ResponseEntity.ok("Clinic deleted successfully");
    }

    @PostMapping("/locations")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> addClinicLocation(
            @RequestParam Long locationId,
            @RequestParam BigDecimal fee,
            @RequestParam boolean isPrimary,
            Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        doctorService.addClinicLocation(userDetails.getId(), locationId, fee, isPrimary);
        return ResponseEntity.ok("Clinic location added successfully");
    }

    // DOCTOR DASHBOARD & APPOINTMENT MANAGEMENT

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorDashboardDTO> getDashboard(Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(doctorService.getDashboardData(userDetails.getId()));
    }

    @GetMapping("/appointments/today")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentDTO>> getTodayAppointments(Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(doctorService.getDashboardData(userDetails.getId()).getTodayAppointments());
    }

    @GetMapping("/appointments")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<AppointmentDTO>> getAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(userDetails.getId(), page, size, date));
    }

    @GetMapping("/appointments/{appointmentId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentDetailsDTO> getAppointmentDetails(
            @PathVariable Long appointmentId, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(appointmentService.getAppointmentDetailsForDoctor(appointmentId, userDetails.getId()));
    }

    @PostMapping("/appointments/{appointmentId}/vitals")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> recordVitals(
            @PathVariable Long appointmentId,
            @RequestBody @Valid PatientVitalRequest request,
            Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        appointmentService.recordVitals(appointmentId, userDetails.getId(), request);
        return ResponseEntity.ok("Vitals recorded successfully");
    }

    @PutMapping("/appointments/{appointmentId}/notes")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> updateNotes(
            @PathVariable Long appointmentId,
            @RequestBody @Valid AppointmentNotesRequest request,
            Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        appointmentService.updateNotes(appointmentId, userDetails.getId(), request);
        return ResponseEntity.ok("Notes updated successfully");
    }

    @PostMapping("/appointments/{appointmentId}/lab-tests")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> orderLabTest(
            @PathVariable Long appointmentId,
            @RequestBody @Valid LabTestRequest request,
            Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        appointmentService.orderLabTest(appointmentId, userDetails.getId(), request);
        return ResponseEntity.ok("Lab test ordered successfully");
    }

    @PutMapping("/appointments/{appointmentId}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentDTO> updateStatus(
            @PathVariable Long appointmentId,
            @RequestBody @Valid AppointmentUpdateDTO request,
            Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(appointmentId, userDetails.getId(), request.getStatus()));
    }
}
