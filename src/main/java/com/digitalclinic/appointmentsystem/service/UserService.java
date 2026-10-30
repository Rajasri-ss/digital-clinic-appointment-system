package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.LoginDTO;
import com.digitalclinic.appointmentsystem.dto.UserRegistrationDTO;
import com.digitalclinic.appointmentsystem.model.Doctor;
import com.digitalclinic.appointmentsystem.model.Patient;
import com.digitalclinic.appointmentsystem.model.Role;
import com.digitalclinic.appointmentsystem.model.User;
import com.digitalclinic.appointmentsystem.repository.DoctorRepository;
import com.digitalclinic.appointmentsystem.repository.PatientRepository;
import com.digitalclinic.appointmentsystem.repository.UserRepository;
import com.digitalclinic.appointmentsystem.security.JwtTokenProvider;
import com.digitalclinic.appointmentsystem.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Map<String, String> registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        if (userRepository.existsByPhone(registrationDTO.getPhone())) {
            throw new RuntimeException("Error: Phone number is already in use!");
        }

        // Split fullName into firstName and lastName
        String[] nameParts = registrationDTO.getFullName().trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // Create new user's account
                User user = User.builder()
                        .username(registrationDTO.getEmail())
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(registrationDTO.getEmail())
                        .phone(registrationDTO.getPhone())
                        .password(passwordEncoder.encode(registrationDTO.getPassword()))
                        .role(registrationDTO.getRole())
                        .isActive(true)
                        .build();

        user = userRepository.save(user);

        // Save respective profile
        if (registrationDTO.getRole() == Role.PATIENT) {
            LocalDate dob = null;
            if (registrationDTO.getDob() != null && !registrationDTO.getDob().isEmpty()) {
                dob = LocalDate.parse(registrationDTO.getDob());
            }

            Patient patient = Patient.builder()
                    .user(user)
                    .fullName(registrationDTO.getFullName())
                    .gender(registrationDTO.getGender())
                    .bloodGroup(registrationDTO.getBloodGroup())
                    .dateOfBirth(dob)
                    .address(registrationDTO.getAddress())
                    .city(registrationDTO.getCity())
                    .state(registrationDTO.getState())
                    .pincode(registrationDTO.getPincode())
                    .build();
            patientRepository.save(patient);
        } else if (registrationDTO.getRole() == Role.DOCTOR) {
            Doctor doctor = Doctor.builder()
                    .user(user)
                    .fullName(registrationDTO.getFullName())
                    .specialization("General") // Default, to be updated later by admin/doctor
                    .build();
            doctorRepository.save(doctor);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully!");
        return response;
    }

    public Map<String, Object> authenticateUser(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getIdentifier(), loginDTO.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseGet(() -> userRepository.findByPhone(userDetails.getUsername()).orElseThrow());

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().name());

        return response;
    }
}
