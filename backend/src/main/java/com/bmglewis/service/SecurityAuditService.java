package com.bmglewis.service;

import com.bmglewis.model.SecurityAuditLog;
import com.bmglewis.repository.SecurityAuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final SecurityAuditLogRepository auditRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSecurityEvent(String email, String eventType, HttpServletRequest request,
                                 boolean successful, String failureReason) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .email(email)
                    .eventType(eventType)
                    .ipAddress(getClientIP(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .timestamp(LocalDateTime.now())
                    .successful(successful)
                    .failureReason(failureReason)
                    .build();

            auditRepository.save(auditLog);
            log.info("Security event logged: {} for {}", eventType, email);
        } catch (Exception e) {
            log.error("Failed to log security event: {}", e.getMessage());
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSecurityEvent(String email, String eventType, HttpServletRequest request,
                                 boolean successful, String failureReason, String additionalInfo) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .email(email)
                    .eventType(eventType)
                    .ipAddress(getClientIP(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .timestamp(LocalDateTime.now())
                    .successful(successful)
                    .failureReason(failureReason)
                    .additionalInfo(additionalInfo)
                    .build();

            auditRepository.save(auditLog);
            log.info("Security event logged: {} for {}", eventType, email);
        } catch (Exception e) {
            log.error("Failed to log security event: {}", e.getMessage());
        }
    }

    public long countRecentFailedLogins(String email, int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        return auditRepository.countFailedAttempts(email, SecurityAuditLog.EVENT_FAILED_LOGIN, since);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}