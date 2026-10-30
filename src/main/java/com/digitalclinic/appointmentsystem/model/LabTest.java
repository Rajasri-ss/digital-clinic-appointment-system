package com.digitalclinic.appointmentsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lab_tests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor; // Doctor who prescribed it

    @Column(name = "test_name", nullable = false, length = 150)
    private String testName;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // PENDING, COMPLETED, CANCELLED

    @Lob
    @Column(name = "result")
    private String result;

    @Column(name = "file_url")
    private String fileUrl; // URL to the uploaded PDF/Image report

    @Lob
    @Column(name = "remarks")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;
}
