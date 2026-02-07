package com.bmglewis.service;

import com.bmglewis.model.AuditLog;
import com.bmglewis.model.User;
import com.bmglewis.repository.AuditLogRepository;
import com.bmglewis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FILE TYPE: CLASS (@Service)
 * Audit logging service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void log(String action, String entityType, Long entityId, String description) {
        User currentUser = getCurrentUser();

        AuditLog auditLog = AuditLog.builder()
                .user(currentUser)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Audit log created: {} - {}", action, description);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
}