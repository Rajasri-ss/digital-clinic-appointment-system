package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.AdminUserDTO;
import com.digitalclinic.appointmentsystem.dto.DoctorAdminDTO;
import com.digitalclinic.appointmentsystem.model.Doctor;
import com.digitalclinic.appointmentsystem.model.Patient;
import com.digitalclinic.appointmentsystem.model.Role;
import com.digitalclinic.appointmentsystem.model.User;
import com.digitalclinic.appointmentsystem.repository.DoctorRepository;
import com.digitalclinic.appointmentsystem.repository.PatientRepository;
import com.digitalclinic.appointmentsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserService.class);

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public Page<AdminUserDTO> getAllUsers(int page, int size) {
        logger.debug("Fetching all users - page: {}, size: {}", page, size);
        Page<User> users = userRepository.findAll(PageRequest.of(page, size));
        return users.map(this::convertAdminToDTO);
    }

    public Page<Patient> getAllPatients(int page, int size) {
        logger.debug("Fetching all patients - page: {}, size: {}", page, size);
        return patientRepository.findAll(PageRequest.of(page, size));
    }

    public Page<DoctorAdminDTO> getAllDoctors(int page, int size) {
        logger.debug("Fetching all doctors - page: {}, size: {}", page, size);
        Page<Doctor> doctors = doctorRepository.findAll(PageRequest.of(page, size));
        return doctors.map(this::convertToDTO);
    }

    public Page<AdminUserDTO> getAllAdmins(int page, int size) {
        logger.debug("Fetching all admins - page: {}, size: {}", page, size);
        Page<User> admins = userRepository.findByRole(Role.ADMIN, PageRequest.of(page, size));
        return admins.map(this::convertAdminToDTO);
    }

    private DoctorAdminDTO convertToDTO(Doctor doctor) {
        logger.debug("Converting doctor {} to DTO", doctor.getId());
        DoctorAdminDTO.UserBasicDTO userDTO = null;
        if (doctor.getUser() != null) {
            userDTO = DoctorAdminDTO.UserBasicDTO.builder()
                    .id(doctor.getUser().getId())
                    .email(doctor.getUser().getEmail())
                    .phone(doctor.getUser().getPhone())
                    .active(doctor.getUser().isActive())
                    .createdAt(doctor.getUser().getCreatedAt())
                    .build();
            logger.debug("User DTO created for doctor {}: email={}", doctor.getId(), userDTO.getEmail());
        }

        DoctorAdminDTO dto = DoctorAdminDTO.builder()
                .id(doctor.getId())
                .fullName(doctor.getFullName())
                .specialty(doctor.getSpecialty())
                .specialization(doctor.getSpecialization())
                .qualifications(doctor.getQualifications())
                .licenseNumber(doctor.getLicenseNumber())
                .experienceYears(doctor.getExperienceYears())
                .about(doctor.getAbout())
                .profilePhoto(doctor.getProfilePhoto())
                .languagesSpoken(doctor.getLanguagesSpoken())
                .consultationFee(doctor.getConsultationFee())
                .averageRating(doctor.getAverageRating())
                .totalReviews(doctor.getTotalReviews())
                .isAvailable(doctor.getAvailable() != null ? doctor.getAvailable() : false)
                .isVerified(doctor.getVerified() != null ? doctor.getVerified() : false)
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .user(userDTO)
                .build();
        logger.debug("Doctor DTO created for doctor {}: verified={}, available={}", doctor.getId(), dto.getIsVerified(), dto.getIsAvailable());
        return dto;
    }

    private AdminUserDTO convertAdminToDTO(User user) {
        logger.debug("Converting admin user {} to DTO", user.getId());
        return AdminUserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .active(user.isActive())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        logger.debug("Toggling user status for userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id: {}", userId);
                    return new RuntimeException("User not found");
                });
        user.setActive(!user.isActive());
        userRepository.save(user);
        logger.info("User status toggled for userId: {} - new status: {}", userId, user.isActive());
    }

    @Transactional
    public void approveDoctor(Long doctorId) {
        logger.debug("Approving doctor with id: {}", doctorId);
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> {
                    logger.error("Doctor not found with id: {}", doctorId);
                    return new RuntimeException("Doctor not found");
                });
        doctor.setIsVerified(true);
        doctor.setIsAvailable(true);
        doctorRepository.save(doctor);
        logger.info("Doctor approved: id={}, verified={}, available={}", doctorId, doctor.getVerified(), doctor.getAvailable());
    }

    @Transactional
    public void rejectDoctor(Long doctorId) {
        logger.debug("Rejecting doctor with id: {}", doctorId);
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> {
                    logger.error("Doctor not found with id: {}", doctorId);
                    return new RuntimeException("Doctor not found");
                });
        doctor.setIsVerified(false);
        doctor.setIsAvailable(false);
        doctorRepository.save(doctor);
        logger.info("Doctor rejected: id={}, verified={}, available={}", doctorId, doctor.getVerified(), doctor.getAvailable());
    }

    @Transactional
    public void suspendDoctor(Long doctorId) {
        logger.debug("Suspending doctor with id: {}", doctorId);
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> {
                    logger.error("Doctor not found with id: {}", doctorId);
                    return new RuntimeException("Doctor not found");
                });
        doctor.setIsVerified(false);
        doctor.setIsAvailable(false);
        doctorRepository.save(doctor);
        logger.info("Doctor suspended: id={}, verified={}, available={}", doctorId, doctor.getVerified(), doctor.getAvailable());
    }

    // Get single doctor by ID
    public DoctorAdminDTO getDoctorById(Long doctorId) {
        logger.debug("Fetching doctor by id: {}", doctorId);
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> {
                    logger.error("Doctor not found with id: {}", doctorId);
                    return new RuntimeException("Doctor not found");
                });
        return convertToDTO(doctor);
    }

    // Get single patient by ID
    public Patient getPatientById(Long patientId) {
        logger.debug("Fetching patient by id: {}", patientId);
        return patientRepository.findById(patientId)
                .orElseThrow(() -> {
                    logger.error("Patient not found with id: {}", patientId);
                    return new RuntimeException("Patient not found");
                });
    }

    // Get single user by ID
    public AdminUserDTO getUserById(Long userId) {
        logger.debug("Fetching user by id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id: {}", userId);
                    return new RuntimeException("User not found");
                });
        return convertAdminToDTO(user);
    }
}
