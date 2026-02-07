package com.bmglewis.repository;

import com.bmglewis.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FILE TYPE: INTERFACE (Repository)
 * Audit log repository
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUser_UserId(Long userId, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    List<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}