package com.digitalclinic.appointmentsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "patient_vitals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientVital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vital_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "blood_pressure", length = 20)
    private String bloodPressure;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "height")
    private Double height;

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(name = "blood_sugar", length = 20)
    private String bloodSugar;

    @Column(name = "oxygen_saturation")
    private Integer oxygenSaturation;

    @Lob
    @Column(name = "notes")
    private String notes;

    @CreationTimestamp
    @Column(name = "record_date", updatable = false)
    private LocalDateTime recordDate;

    // Optional relation to Doctor or User who recorded it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;
}
