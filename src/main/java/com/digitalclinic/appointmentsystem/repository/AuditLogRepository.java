package com.digitalclinic.appointmentsystem.repository;

import com.digitalclinic.appointmentsystem.model.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AdminAuditLog, Long> {
    Page<AdminAuditLog> findByAdminUser_Id(Long adminUserId, Pageable pageable);
}
