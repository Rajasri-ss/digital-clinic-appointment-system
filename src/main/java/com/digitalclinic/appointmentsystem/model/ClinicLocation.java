package com.digitalclinic.appointmentsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "clinic_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long id;

    @Column(name = "clinic_name", nullable = false, length = 100)
    private String clinicName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 20)
    private String pincode;

    @Column(length = 20)
    private String phone;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(columnDefinition = "TEXT")
    private String facilities;

    @Column(name = "operating_hours", columnDefinition = "TEXT")
    private String operatingHours;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships to be added when entities exist
    // @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    // private List<DoctorLocation> doctorLocations;

    // @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    // private List<DoctorSchedule> schedules;
}
