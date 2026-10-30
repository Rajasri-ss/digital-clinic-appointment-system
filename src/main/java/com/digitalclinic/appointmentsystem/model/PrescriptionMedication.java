package com.digitalclinic.appointmentsystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "prescription_medications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionMedication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medication_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Prescription prescription;

    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;

    @Column(name = "dosage", nullable = false, length = 50)
    private String dosage; // e.g., 500mg

    @Column(name = "frequency", nullable = false, length = 100)
    private String frequency; // e.g., Twice a day (1-0-1)

    @Column(name = "duration", length = 50)
    private String duration; // e.g., 5 days

    @Column(name = "timing", length = 100)
    private String timing; // e.g., After food

    @Column(name = "instructions", length = 255)
    private String instructions; // Additional notes
}
