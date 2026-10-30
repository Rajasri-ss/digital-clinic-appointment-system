package com.digitalclinic.appointmentsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctor_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be less than 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Size(max = 100, message = "Specialty must be less than 100 characters")
    @Column(name = "specialty")
    private String specialty;

    @NotBlank(message = "Specialization is required")
    @Size(max = 100, message = "Specialization must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String specialization;

    @Column(name = "hospital_id")
    private Long hospitalId;

    @Column(name = "hospital_name", length = 100)
    private String hospitalName;

    @Column(name = "hospital_address", columnDefinition = "TEXT")
    private String hospitalAddress;

    @Column(columnDefinition = "TEXT")
    private String qualifications;

    @Column(name = "license_number", unique = true, length = 100)
    private String licenseNumber;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(columnDefinition = "TEXT")
    private String about;

    @Column(name = "profile_photo")
    private String profilePhoto;

    @Column(name = "languages_spoken")
    private String languagesSpoken;

    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    // Getters for Boolean fields (Lombok generates getIsAvailable/getIsVerified)
    public Boolean getAvailable() {
        return isAvailable;
    }

    public Boolean getVerified() {
        return isVerified;
    }

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships to be added when entities exist
    // @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    // private List<DoctorLocation> doctorLocations;

    // @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    // private List<DoctorSchedule> schedules;

    // @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    // private List<Appointment> appointments;

    // @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    // private List<Review> reviews;
}
