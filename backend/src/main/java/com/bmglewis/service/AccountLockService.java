package com.bmglewis.service;

import com.bmglewis.exception.AccountLockedException;
import com.bmglewis.model.SecurityAuditLog;
import com.bmglewis.model.User;
import com.bmglewis.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class AccountLockService {

    private final UserRepository userRepository;
    private final SecurityAuditService auditService;

    @Value("${security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Transactional
    public void recordFailedLogin(User user, HttpServletRequest request) {
        user.incrementFailedAttempts();

        if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
            user.lockAccount();
            userRepository.save(user);

            auditService.logSecurityEvent(
                    user.getEmail(),
                    SecurityAuditLog.EVENT_ACCOUNT_LOCKED,
                    request,
                    true,
                    null,
                    "Account locked after " + maxFailedAttempts + " failed login attempts"
            );

            log.warn("Account locked for user: {} after {} failed attempts",
                    user.getEmail(), maxFailedAttempts);
        } else {
            userRepository.save(user);
            log.info("Failed login attempt {} of {} for user: {}",
                    user.getFailedLoginAttempts(), maxFailedAttempts, user.getEmail());
        }
    }

    @Transactional
    public void recordSuccessfulLogin(User user) {
        user.resetFailedAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Successful login for user: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public void checkAccountLock(User user) {
        if (user.getAccountLocked() != null && user.getAccountLocked()) {
            if (user.getLockTime() != null) {
                LocalDateTime unlockTime = user.getLockTime().plusMinutes(lockoutDurationMinutes);

                if (LocalDateTime.now().isBefore(unlockTime)) {
                    long minutesRemaining = java.time.Duration.between(
                            LocalDateTime.now(), unlockTime
                    ).toMinutes();

                    throw new AccountLockedException(
                            String.format("Account is locked. Please try again in %d minutes.", minutesRemaining)
                    );
                } else {
                    // Lock has expired, will be reset on successful login
                    log.info("Account lock expired for user: {}", user.getEmail());
                }
            } else {
                throw new AccountLockedException("Account is locked. Please contact support.");
            }
        }
    }

    @Transactional
    public void unlockAccount(String email, HttpServletRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.resetFailedAttempts();
        userRepository.save(user);

        auditService.logSecurityEvent(
                email,
                SecurityAuditLog.EVENT_ACCOUNT_UNLOCKED,
                request,
                true,
                null,
                "Account manually unlocked"
        );

        log.info("Account unlocked for user: {}", email);
    }

    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public int getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }
}