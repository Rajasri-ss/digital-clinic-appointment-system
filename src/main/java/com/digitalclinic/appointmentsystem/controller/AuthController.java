package com.digitalclinic.appointmentsystem.controller;

import com.digitalclinic.appointmentsystem.dto.LoginDTO;
import com.digitalclinic.appointmentsystem.dto.UserRegistrationDTO;
import com.digitalclinic.appointmentsystem.model.Doctor;
import com.digitalclinic.appointmentsystem.model.Patient;
import com.digitalclinic.appointmentsystem.model.Role;
import com.digitalclinic.appointmentsystem.model.User;
import com.digitalclinic.appointmentsystem.repository.DoctorRepository;
import com.digitalclinic.appointmentsystem.repository.PatientRepository;
import com.digitalclinic.appointmentsystem.repository.UserRepository;
import com.digitalclinic.appointmentsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @GetMapping("/test-hash")
    public ResponseEntity<?> testHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        return ResponseEntity.ok(Map.of("hash", hash, "password", password));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO registrationDTO) {
        try {
            Map<String, String> response = userService.registerUser(registrationDTO);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginDTO loginDTO) {
        try {
            Map<String, Object> response = userService.authenticateUser(loginDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bad credentials or user not found"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            // Extract authenticated user from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
            }

            String username = authentication.getName(); // This is the email or phone used for login

            // Find user by email or phone
            Optional<User> userOpt = userRepository.findByEmail(username);
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByPhone(username);
            }

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            User user = userOpt.get();
            Map<String, Object> profile = new HashMap<>();
            profile.put("email", user.getEmail());
            profile.put("phone", user.getPhone());
            profile.put("role", user.getRole().toString());
            profile.put("isActive", user.isActive());

            // Based on role, fetch profile details
            if (user.getRole() == Role.PATIENT) {
                Optional<Patient> patientOpt = patientRepository.findByUser_Id(user.getId());
                if (patientOpt.isPresent()) {
                    Patient patient = patientOpt.get();
                    profile.put("fullName", patient.getFullName());
                    profile.put("dob", patient.getDateOfBirth());
                    profile.put("gender", patient.getGender());
                    profile.put("bloodGroup", patient.getBloodGroup());
                    profile.put("address", patient.getAddress());
                    profile.put("city", patient.getCity());
                    profile.put("state", patient.getState());
                    profile.put("pincode", patient.getPincode());
                } else {
                    profile.put("fullName", "Patient");
                }
            } else if (user.getRole() == Role.DOCTOR) {
                Optional<Doctor> doctorOpt = doctorRepository.findByUser_Id(user.getId());
                if (doctorOpt.isPresent()) {
                    Doctor doctor = doctorOpt.get();
                    profile.put("fullName", doctor.getFullName());
                    profile.put("specialization", doctor.getSpecialization());
                    profile.put("qualifications", doctor.getQualifications());
                    profile.put("experienceYears", doctor.getExperienceYears());
                    profile.put("consultationFee", doctor.getConsultationFee());
                    profile.put("isAvailable", doctor.getAvailable());
                    profile.put("isVerified", doctor.getVerified());
                } else {
                    profile.put("fullName", "Doctor");
                }
            } else if (user.getRole() == Role.ADMIN) {
                // Admin users may not have Patient/Doctor profile
                profile.put("fullName", "Admin");
            }

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch profile: " + e.getMessage()));
        }
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<?> registerDoctor(@RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("password") String password,
            @RequestParam("specialization") String specialization,
            @RequestParam("licenseNumber") String licenseNumber,
            @RequestParam(value = "qualifications", required = false) String qualifications,
            @RequestParam(value = "experienceYears", required = false, defaultValue = "0") Integer experienceYears,
            @RequestParam(value = "languagesSpoken", required = false) String languagesSpoken,
            @RequestParam(value = "consultationFee", required = false, defaultValue = "0") Double consultationFee,
            @RequestParam(value = "about", required = false) String about,
            @RequestParam(value = "profilePhoto", required = false) org.springframework.web.multipart.MultipartFile profilePhoto) {
        logger.debug("Doctor registration attempt - email: {}, specialization: {}", email, specialization);

        // Validate required fields
        if (fullName == null || fullName.trim().isEmpty()) {
            logger.error("Doctor registration failed - full name is required");
            return ResponseEntity.badRequest().body(Map.of("error", "Full name is required"));
        }

        if (specialization == null || specialization.trim().isEmpty()) {
            logger.error("Doctor registration failed - specialization is required");
            return ResponseEntity.badRequest().body(Map.of("error", "Specialization is required"));
        }

        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            logger.error("Doctor registration failed - license number is required");
            return ResponseEntity.badRequest().body(Map.of("error", "License number is required"));
        }

        try {
            // Check if email or phone already exists
            if (userRepository.existsByEmail(email)) {
                logger.error("Doctor registration failed - email already in use: {}", email);
                return ResponseEntity.badRequest().body(Map.of("error", "Email is already in use!"));
            }
            if (userRepository.existsByPhone(phone)) {
                logger.error("Doctor registration failed - phone already in use: {}", phone);
                return ResponseEntity.badRequest().body(Map.of("error", "Phone number is already in use!"));
            }

            // Split fullName into firstName and lastName
            String[] nameParts = fullName.trim().split("\\s+", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            // Create user account
            User user = User.builder()
                    .username(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .phone(phone)
                    .password(passwordEncoder.encode(password))
                    .role(Role.DOCTOR)
                    .isActive(true)
                    .build();
            user = userRepository.save(user);
            logger.info("User created for doctor: {}", email);

            // Handle profile photo upload (optional - save to uploads folder or just store
            // filename)
            String photoPath = null;
            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                // For now, just store the original filename
                // In production, you'd save the file to disk/cloud and store the path
                photoPath = "/uploads/doctors/" + user.getId() + "_" + profilePhoto.getOriginalFilename();
                // TODO: Implement actual file saving logic
            }

            // Create doctor profile
            Doctor doctor = Doctor.builder()
                    .user(user)
                    .fullName(fullName.trim())
                    .specialty(specialization.trim()) // Map to specialty column
                    .specialization(specialization.trim()) // Map to specialization column
                    .licenseNumber(licenseNumber.trim())
                    .qualifications(qualifications != null ? qualifications.trim() : null)
                    .experienceYears(experienceYears)
                    .languagesSpoken(languagesSpoken != null ? languagesSpoken.trim() : null)
                    .consultationFee(consultationFee != null ? java.math.BigDecimal.valueOf(consultationFee) : null)
                    .about(about != null ? about.trim() : null)
                    .profilePhoto(photoPath)
                    .isVerified(false) // Doctors need admin verification
                    .isAvailable(false) // Not available until verified
                    .build();
            doctorRepository.save(doctor);
            logger.info("Doctor profile created: id={}, specialization={}", doctor.getId(), specialization);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Doctor registration successful! Your account is pending verification.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Doctor registration failed for email {}: {}", email, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

}
