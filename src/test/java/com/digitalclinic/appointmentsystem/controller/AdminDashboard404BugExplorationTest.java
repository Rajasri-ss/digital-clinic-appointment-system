package com.digitalclinic.appointmentsystem.controller;

import com.digitalclinic.appointmentsystem.model.Role;
import com.digitalclinic.appointmentsystem.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Bug Condition Exploration Test for Admin Dashboard 404 Errors
 * 
 * **Validates: Requirements 2.1, 2.2, 2.3**
 * 
 * **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
 * **EXPECTED OUTCOME**: Test FAILS with 404 errors (this is correct - it proves the bug exists)
 * 
 * This test encodes the expected behavior:
 * - Property 1: Bug Condition - Missing Endpoints Return 404
 * - When the fix is implemented, this test will pass and validate the fix
 * 
 * Bug Condition: isBugCondition where:
 * - path='/api/auth/profile' AND method='GET' AND endpointNotMapped
 * - path='/api/admin/analytics/dashboard' AND method='GET' AND endpointNotMapped
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AdminDashboard404BugExplorationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String patientToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Generate valid JWT token for a patient user
        UserDetails patientUser = User.builder()
                .username("patient@test.com")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_PATIENT")))
                .build();
        Authentication patientAuth = new UsernamePasswordAuthenticationToken(
                patientUser, null, patientUser.getAuthorities());
        patientToken = jwtTokenProvider.generateToken(patientAuth);

        // Generate valid JWT token for an admin user
        UserDetails adminUser = User.builder()
                .username("admin@test.com")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                adminUser, null, adminUser.getAuthorities());
        adminToken = jwtTokenProvider.generateToken(adminAuth);
    }

    /**
     * Test Case 1: Profile Endpoint Bug Condition
     * 
     * Bug Condition: GET /api/auth/profile with valid JWT token returns 404
     * Expected Behavior (after fix): Should return 200 with user profile data including fullName
     * 
     * **ON UNFIXED CODE**: This test will FAIL with 404 (proves bug exists)
     * **ON FIXED CODE**: This test will PASS with 200 (proves fix works)
     */
    @Test
    void testProfileEndpoint_WithValidToken_ShouldReturn200WithUserProfile() throws Exception {
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.role").exists());
    }

    /**
     * Test Case 2: Dashboard Endpoint Bug Condition
     * 
     * Bug Condition: GET /api/admin/analytics/dashboard with valid admin JWT token returns 404
     * Expected Behavior (after fix): Should return 200 with dashboard metrics
     * 
     * **ON UNFIXED CODE**: This test will FAIL with 404 (proves bug exists)
     * **ON FIXED CODE**: This test will PASS with 200 (proves fix works)
     */
    @Test
    void testDashboardEndpoint_WithValidAdminToken_ShouldReturn200WithMetrics() throws Exception {
        mockMvc.perform(get("/api/admin/analytics/dashboard")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPatients").exists())
                .andExpect(jsonPath("$.totalDoctors").exists())
                .andExpect(jsonPath("$.todayAppointments").exists())
                .andExpect(jsonPath("$.totalRevenue").exists());
    }

    /**
     * Test Case 3: Edge Case - Unauthenticated Profile Request
     * 
     * Edge Case Validation: Unauthenticated requests should return 401/403, not 404
     * This confirms the bug is specifically about missing endpoints, not auth issues
     * 
     * **EXPECTED**: This test should PASS even on unfixed code (returns 401/403)
     */
    @Test
    void testProfileEndpoint_WithoutToken_ShouldReturn401Or403NotFound() throws Exception {
        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test Case 4: Edge Case - Non-Admin Dashboard Request
     * 
     * Edge Case Validation: Non-admin requests should return 403, not 404
     * This confirms the bug is specifically about missing endpoints, not auth issues
     * 
     * **ON UNFIXED CODE**: This test will FAIL with 404 (endpoint doesn't exist)
     * **ON FIXED CODE**: This test will PASS with 403 (endpoint exists but access denied)
     */
    @Test
    void testDashboardEndpoint_WithPatientToken_ShouldReturn403Forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/analytics/dashboard")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    /**
     * Test Case 5: Edge Case - Unauthenticated Dashboard Request
     * 
     * Edge Case Validation: Unauthenticated requests should return 401/403, not 404
     * 
     * **EXPECTED**: This test should PASS even on unfixed code (returns 401/403)
     */
    @Test
    void testDashboardEndpoint_WithoutToken_ShouldReturn401Or403NotFound() throws Exception {
        mockMvc.perform(get("/api/admin/analytics/dashboard"))
                .andExpect(status().isUnauthorized());
    }
}
