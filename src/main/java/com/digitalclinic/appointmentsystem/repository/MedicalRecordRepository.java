package com.digitalclinic.appointmentsystem.repository;

import com.digitalclinic.appointmentsystem.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatientIdOrderByUploadDateDesc(Long patientId);
}
