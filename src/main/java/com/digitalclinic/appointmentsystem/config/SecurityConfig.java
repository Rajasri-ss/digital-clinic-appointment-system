package com.digitalclinic.appointmentsystem.config;

import com.digitalclinic.appointmentsystem.security.JwtAuthenticationFilter;
import com.digitalclinic.appointmentsystem.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        // Public API endpoints (no auth required)
                        .requestMatchers("/api/hospitals/**").permitAll()
                        .requestMatchers("/api/doctors/available").permitAll()
                        .requestMatchers("/api/doctors/search").permitAll()
                        .requestMatchers("/api/doctors/hospital/**").permitAll()
                        .requestMatchers("/api/doctors/{id}").permitAll()
                        .requestMatchers("/api/doctors/specialization/**").permitAll()
                        .requestMatchers("/api/doctors/top-rated").permitAll()
                        // Redirect endpoints (without .html)
                        .requestMatchers("/login", "/register", "/dashboard", "/admin", "/doctor",
                                "/register-patient", "/register-doctor").permitAll()
                        // Static HTML pages
                        .requestMatchers("/", "/index.html", "/login.html", "/register.html",
                                "/register_patient.html", "/register_doctor.html",
                                "/forgot_password.html", "/patient_dashboard.html", "/doctor_dashboard.html",
                                "/appointment_booking.html", "/appointment_list.html", "/appointment_details.html",
                                "/emergency_appointment.html", "/app_updation.html", "/medical_records.html",
                                "/prescription_view.html", "/doctor_patients.html", "/doctor_patient_history.html",
                                "/checkout.html", "/admin_dashboard.html", "/admin_users.html",
                                "/admin_reports.html", "/admin_settings.html", "/doctor_profile.html",
                                "/doctor_schedule.html", "/doctor_search.html", "/profile.html").permitAll()
                        // Static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**").permitAll()
                        .requestMatchers("/*.css", "/*.js", "/*.png", "/*.jpg", "/*.jpeg", "/*.gif", "/*.svg", "/*.ico").permitAll()
                        // WebSocket
                        .requestMatchers("/ws/**", "/app/**", "/topic/**", "/user/**").permitAll()
                        // Error page
                        .requestMatchers("/error").permitAll()
                        // All other requests need authentication
                        .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); // Allow all for dev, restrict in prod
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}