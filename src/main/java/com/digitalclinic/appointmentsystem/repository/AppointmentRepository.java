package com.digitalclinic.appointmentsystem.repository;

import com.digitalclinic.appointmentsystem.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

        Optional<Appointment> findByBookingReference(String bookingReference);

        Page<Appointment> findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(Long patientId,
                        Pageable pageable);

        Page<Appointment> findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(Long doctorId, Pageable pageable);

        @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date ORDER BY a.appointmentTime ASC")
        List<Appointment> findDoctorAppointmentsForDate(@Param("doctorId") Long doctorId,
                        @Param("date") LocalDate date);

        @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.status = :status ORDER BY a.appointmentTime ASC")
        List<Appointment> findDoctorAppointmentsByDateAndStatus(
                        @Param("doctorId") Long doctorId,
                        @Param("date") LocalDate date,
                        @Param("status") Appointment.AppointmentStatus status);

        long countByPatientIdAndStatus(Long patientId, Appointment.AppointmentStatus status);

        long countByDoctorIdAndAppointmentDateAndStatus(Long doctorId, LocalDate date,
                        Appointment.AppointmentStatus status);

        long countByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);

        long countByDoctorIdAndStatus(Long doctorId, Appointment.AppointmentStatus status);

        @Query("SELECT COUNT(DISTINCT a.patient.id) FROM Appointment a WHERE a.doctor.id = :doctorId")
        long countDistinctPatientsByDoctorId(@Param("doctorId") Long doctorId);

        @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.isEmergency = true AND a.status = 'SCHEDULED'")
        List<Appointment> findEmergencyAppointments(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

        List<Appointment> findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(Long doctorId, LocalDate date);

        Page<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date, Pageable pageable);

        boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                        Long doctorId, LocalDate date, LocalTime time, Appointment.AppointmentStatus status);

        List<Appointment> findByPatientId(Long patientId);

        List<Appointment> findByDoctorId(Long doctorId);

        List<Appointment> findByAppointmentDateBetween(LocalDate startDate, LocalDate endDate);

        List<Appointment> findByStatus(Appointment.AppointmentStatus status);

        List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate appointmentDate);

        @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.location.id = :locationId AND a.appointmentDate = :date AND a.status != 'CANCELLED' ORDER BY a.appointmentTime ASC")
        List<Appointment> findBookedSlots(@Param("doctorId") Long doctorId, @Param("locationId") Long locationId, @Param("date") LocalDate date);

        List<Appointment> findByIsEmergencyTrue();

        long countByDoctorIdAndAppointmentDateAndAppointmentTimeBetweenAndStatusNot(
                        Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime,
                        Appointment.AppointmentStatus status);

        // Patient dashboard queries
        long countByPatientId(Long patientId);

        @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate >= CURRENT_DATE AND a.status IN ('SCHEDULED', 'CONFIRMED')")
        long countUpcomingAppointmentsByPatientId(@Param("patientId") Long patientId);

        @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId AND a.status = 'COMPLETED'")
        long countCompletedAppointmentsByPatientId(@Param("patientId") Long patientId);

        @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId AND a.status = 'CANCELLED'")
        long countCancelledAppointmentsByPatientId(@Param("patientId") Long patientId);

        @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate >= CURRENT_DATE AND a.status IN ('SCHEDULED', 'CONFIRMED') ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
        List<Appointment> findUpcomingAppointmentsByPatientId(@Param("patientId") Long patientId);
}
