package com.digitalclinic.appointmentsystem.repository;

import com.digitalclinic.appointmentsystem.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUser_Id(Long userId);

    Optional<Patient> findByUser_Email(String email);

    Optional<Patient> findByUser_Phone(String phone);

    List<Patient> findByFullNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM Patient p WHERE p.createdAt >= :fromDate")
    List<Patient> findRecentPatients(@Param("fromDate") LocalDateTime fromDate);

    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
