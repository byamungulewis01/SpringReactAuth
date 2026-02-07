package com.bmglewis.repository;

import com.bmglewis.model.SecurityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {

    List<SecurityAuditLog> findByEmailOrderByTimestampDesc(String email);

    List<SecurityAuditLog> findByEventTypeOrderByTimestampDesc(String eventType);

    @Query("SELECT sal FROM SecurityAuditLog sal WHERE sal.email = :email AND sal.timestamp > :since ORDER BY sal.timestamp DESC")
    List<SecurityAuditLog> findRecentByEmail(@Param("email") String email, @Param("since") LocalDateTime since);

    @Query("SELECT sal FROM SecurityAuditLog sal WHERE sal.eventType = :eventType AND sal.timestamp > :since ORDER BY sal.timestamp DESC")
    List<SecurityAuditLog> findRecentByEventType(@Param("eventType") String eventType, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(sal) FROM SecurityAuditLog sal WHERE sal.email = :email AND sal.eventType = :eventType AND sal.successful = false AND sal.timestamp > :since")
    long countFailedAttempts(@Param("email") String email, @Param("eventType") String eventType, @Param("since") LocalDateTime since);

    @Query("SELECT sal FROM SecurityAuditLog sal WHERE sal.ipAddress = :ipAddress AND sal.timestamp > :since ORDER BY sal.timestamp DESC")
    List<SecurityAuditLog> findByIpAddressSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
}