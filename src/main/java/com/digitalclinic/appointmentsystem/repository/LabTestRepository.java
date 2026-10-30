package com.digitalclinic.appointmentsystem.repository;

import com.digitalclinic.appointmentsystem.model.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long> {
    List<LabTest> findByPatientIdOrderByCreatedDateDesc(Long patientId);

    List<LabTest> findByDoctorIdOrderByCreatedDateDesc(Long doctorId);
}
