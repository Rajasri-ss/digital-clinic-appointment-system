package com.digitalclinic.appointmentsystem.repository;

import com.digitalclinic.appointmentsystem.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientIdOrderByPrescriptionDateDesc(Long patientId);

    List<Prescription> findByDoctorIdOrderByPrescriptionDateDesc(Long doctorId);

    // Optional: find by appointment id
    List<Prescription> findByAppointmentId(Long appointmentId);
}
