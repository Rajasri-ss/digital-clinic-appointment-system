package com.digitalclinic.appointmentsystem.repository;

import com.digitalclinic.appointmentsystem.model.PrescriptionMedication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionMedicationRepository extends JpaRepository<PrescriptionMedication, Long> {
    List<PrescriptionMedication> findByPrescriptionId(Long prescriptionId);
}
