package com.digitalclinic.appointmentsystem.repository;

import com.digitalclinic.appointmentsystem.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUser_Id(Long userId);

    List<Doctor> findBySpecialization(String specialization);

    List<Doctor> findByIsAvailableTrue();

    List<Doctor> findByIsVerifiedTrue();

    @Query("SELECT d FROM Doctor d WHERE d.isAvailable = true AND d.isVerified = true")
    List<Doctor> findAllAvailableAndVerified();

    // Advanced search
    @Query("SELECT d FROM Doctor d WHERE " +
            "(:specialization IS NULL OR d.specialization = :specialization) AND " +
            "(:minExperience IS NULL OR d.experienceYears >= :minExperience) AND " +
            "(:maxFee IS NULL OR d.consultationFee <= :maxFee) AND " +
            "d.isAvailable = true AND d.isVerified = true")
    Page<Doctor> searchDoctors(
            @Param("specialization") String specialization,
            @Param("minExperience") Integer minExperience,
            @Param("maxFee") BigDecimal maxFee,
            Pageable pageable);

    // Search by name
    List<Doctor> findByFullNameContainingIgnoreCase(String name);

    // Search by hospital
    List<Doctor> findByHospitalId(Long hospitalId);

    // Top rated doctors
    @Query("SELECT d FROM Doctor d WHERE d.averageRating >= :minRating " +
            "ORDER BY d.averageRating DESC, d.totalReviews DESC")
    List<Doctor> findTopRatedDoctors(@Param("minRating") BigDecimal minRating, Pageable pageable);

    Long countBySpecialization(String specialization);
}
