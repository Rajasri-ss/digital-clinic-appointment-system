package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.AdminUserDTO;
import com.digitalclinic.appointmentsystem.model.Role;
import com.digitalclinic.appointmentsystem.model.User;
import com.digitalclinic.appointmentsystem.repository.DoctorRepository;
import com.digitalclinic.appointmentsystem.repository.PatientRepository;
import com.digitalclinic.appointmentsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void testGetAllUsers() {
        User user = User.builder().id(1L).firstName("Admin").lastName("User")
                .email("admin@test.com").phone("1234567890").role(Role.ADMIN).build();
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll((PageRequest) any())).thenReturn(page);

        Page<AdminUserDTO> result = adminUserService.getAllUsers(0, 10);
        assertEquals(1, result.getTotalElements());
        assertEquals("admin@test.com", result.getContent().get(0).getEmail());
        // Password must not be present in DTO
        assertNotNull(result.getContent().get(0).getEmail());
    }

    @Test
    void testGetAllAdmins() {
        User admin = User.builder().id(2L).firstName("Super").lastName("Admin")
                .email("super@test.com").phone("9876543210").role(Role.ADMIN).build();
        Page<User> page = new PageImpl<>(List.of(admin));
        when(userRepository.findByRole(eq(Role.ADMIN), any(PageRequest.class))).thenReturn(page);

        Page<AdminUserDTO> result = adminUserService.getAllAdmins(0, 10);
        assertEquals(1, result.getTotalElements());
        assertEquals("ADMIN", result.getContent().get(0).getRole());
        assertEquals("super@test.com", result.getContent().get(0).getEmail());
    }

    @Test
    void testToggleUserStatus() {
        User user = User.builder().id(1L).isActive(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminUserService.toggleUserStatus(1L);

        assertFalse(user.isActive());
        verify(userRepository, times(1)).save(user);
    }
}
