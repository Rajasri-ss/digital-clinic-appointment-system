package com.digitalclinic.appointmentsystem.repository;

import com.digitalclinic.appointmentsystem.model.ClinicLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicLocationRepository extends JpaRepository<ClinicLocation, Long> {
    List<ClinicLocation> findByCityIgnoreCase(String city);

    List<ClinicLocation> findByCity(String city);

    List<ClinicLocation> findByCityAndState(String city, String state);

    Optional<ClinicLocation> findByClinicName(String clinicName);

    @Query("SELECT cl FROM ClinicLocation cl WHERE " +
            "cl.latitude BETWEEN :minLat AND :maxLat AND " +
            "cl.longitude BETWEEN :minLon AND :maxLon")
    List<ClinicLocation> findNearby(
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLon") BigDecimal minLon,
            @Param("maxLon") BigDecimal maxLon);
}
