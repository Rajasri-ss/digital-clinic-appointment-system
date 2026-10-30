package com.digitalclinic.appointmentsystem.repository;

import com.digitalclinic.appointmentsystem.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    List<Hospital> findByIsActiveTrue();

    List<Hospital> findByCityIgnoreCase(String city);

    @Query("SELECT h FROM Hospital h WHERE h.isActive = true AND " +
            "(LOWER(h.hospitalName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(h.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(h.hospitalAddress) LIKE LOWER(CONCAT('%', :query, '%')))")

    List<Hospital> searchHospitals(@Param("query") String query);
}