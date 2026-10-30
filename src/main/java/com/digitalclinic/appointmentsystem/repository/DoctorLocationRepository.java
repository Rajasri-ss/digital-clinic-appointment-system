package com.digitalclinic.appointmentsystem.repository;

import com.digitalclinic.appointmentsystem.model.DoctorLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorLocationRepository extends JpaRepository<DoctorLocation, Long> {
    List<DoctorLocation> findByDoctorId(Long doctorId);

    List<DoctorLocation> findByLocationId(Long locationId);

    Optional<DoctorLocation> findByDoctorIdAndLocationId(Long doctorId, Long locationId);
}
