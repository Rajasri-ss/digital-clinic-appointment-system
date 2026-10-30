package com.digitalclinic.appointmentsystem.repository;

import com.digitalclinic.appointmentsystem.model.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {
    List<DoctorSchedule> findByDoctorId(Long doctorId);

    List<DoctorSchedule> findByDoctorIdAndIsActiveTrue(Long doctorId);

    List<DoctorSchedule> findByDoctorIdAndLocationId(Long doctorId, Long locationId);

    List<DoctorSchedule> findByDoctorIdAndLocationIdAndIsActiveTrue(Long doctorId, Long locationId);

    @Query("SELECT ds FROM DoctorSchedule ds WHERE " +
            "ds.doctor.id = :doctorId AND " +
            "ds.dayOfWeek = :dayOfWeek AND " +
            "ds.isActive = true")
    Optional<DoctorSchedule> findActiveSchedule(
            @Param("doctorId") Long doctorId,
            @Param("dayOfWeek") DoctorSchedule.DayOfWeek dayOfWeek);

    void deleteByDoctorIdAndLocationId(Long doctorId, Long locationId);

    Optional<DoctorSchedule> findByDoctorIdAndLocationIdAndDayOfWeek(Long doctorId, Long locationId,
            DoctorSchedule.DayOfWeek dayOfWeek);

    @Query("SELECT ds FROM DoctorSchedule ds WHERE " +
            "ds.doctor.id = :doctorId AND " +
            "ds.location.id = :locationId AND " +
            "ds.dayOfWeek = :dayOfWeek AND " +
            "ds.isActive = true")
    Optional<DoctorSchedule> findActiveScheduleByDoctorLocationAndDay(
            @Param("doctorId") Long doctorId,
            @Param("locationId") Long locationId,
            @Param("dayOfWeek") DoctorSchedule.DayOfWeek dayOfWeek);
}
