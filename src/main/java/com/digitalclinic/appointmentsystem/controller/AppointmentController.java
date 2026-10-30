package com.digitalclinic.appointmentsystem.controller;

import com.digitalclinic.appointmentsystem.dto.AppointmentDTO;
import com.digitalclinic.appointmentsystem.dto.AppointmentRequestDTO;
import com.digitalclinic.appointmentsystem.dto.AppointmentUpdateRequestDTO;
import com.digitalclinic.appointmentsystem.dto.AvailableSlotDTO;
import com.digitalclinic.appointmentsystem.dto.EmergencyBookingDTO;
import com.digitalclinic.appointmentsystem.security.UserDetailsImpl;
import com.digitalclinic.appointmentsystem.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // PATIENT ENDPOINTS

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentDTO> bookAppointment(
            @RequestBody @Valid AppointmentRequestDTO requestDTO, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(appointmentService.bookAppointment(userDetails.getId(), requestDTO));
    }

    @PostMapping("/emergency")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentDTO> bookEmergencyAppointment(
            @RequestBody @Valid EmergencyBookingDTO requestDTO, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(appointmentService.bookEmergencyAppointment(userDetails.getId(), requestDTO));
    }

    @GetMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Page<AppointmentDTO>> getMyAppointmentsAsPatient(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(appointmentService.getPatientAppointments(userDetails.getId(), page, size));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentDTO> cancelAppointment(
            @PathVariable Long id, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, userDetails.getId()));
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentDTO> rescheduleAppointment(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime newTime,
            Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(appointmentService.rescheduleAppointment(id, newDate, newTime, userDetails.getId()));
    }

    // DOCTOR ENDPOINTS

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<AppointmentDTO>> getMyAppointmentsAsDoctor(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(userDetails.getId(), page, size));
    }

    @GetMapping("/doctor/schedule")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentDTO>> getDailySchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(appointmentService.getDoctorDailySchedule(userDetails.getId(), date));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(id, userDetails.getId(), status));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentDTO> updateAppointment(
            @PathVariable Long id,
            @RequestBody AppointmentUpdateRequestDTO requestDTO,
            Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(appointmentService.updateAppointment(id, userDetails.getId(), requestDTO));
    }

    // SHARED ENDPOINTS

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<AppointmentDTO> getAppointmentDetails(
            @PathVariable Long id, Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ResponseEntity.ok(appointmentService.getAppointmentDetails(id, userDetails.getId(), role));
    }

    @GetMapping("/slots")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<List<AvailableSlotDTO>> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam Long locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId, locationId, date));
    }

    @GetMapping("/slots/check")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Boolean> hasScheduleForDate(
            @RequestParam Long doctorId,
            @RequestParam Long locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            appointmentService.getAvailableSlots(doctorId, locationId, date);
            return ResponseEntity.ok(true);
        } catch (RuntimeException e) {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping(value = "/{id}/qrcode", produces = org.springframework.http.MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<byte[]> getAppointmentQRCode(@PathVariable Long id, Authentication auth) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
            AppointmentDTO appointment = appointmentService.getAppointmentDetails(id, userDetails.getId(), role);

            String qrContent = "Appointment Ref: " + appointment.getBookingReference() +
                    "\nPatient: " + appointment.getPatientName() +
                    "\nDoctor: " + appointment.getDoctorName() +
                    "\nDate: " + appointment.getAppointmentDate() + " " + appointment.getAppointmentTime();

            com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(qrContent,
                    com.google.zxing.BarcodeFormat.QR_CODE, 200, 200);

            java.io.ByteArrayOutputStream pngOutputStream = new java.io.ByteArrayOutputStream();
            com.google.zxing.client.j2se.MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            return ResponseEntity.ok(pngOutputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
