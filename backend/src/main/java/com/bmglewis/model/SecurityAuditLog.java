package com.bmglewis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "security_audit_log", indexes = {
        @Index(name = "idx_email_timestamp", columnList = "email, timestamp"),
        @Index(name = "idx_event_type", columnList = "eventType"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class SecurityAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String eventType; // LOGIN, LOGOUT, PASSWORD_CHANGE, FAILED_LOGIN, etc.

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Boolean successful;

    @Column(length = 500)
    private String failureReason;

    @Column(length = 1000)
    private String additionalInfo;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // Security event types
    public static final String EVENT_LOGIN = "LOGIN";
    public static final String EVENT_LOGOUT = "LOGOUT";
    public static final String EVENT_FAILED_LOGIN = "FAILED_LOGIN";
    public static final String EVENT_PASSWORD_CHANGE = "PASSWORD_CHANGE";
    public static final String EVENT_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String EVENT_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    public static final String EVENT_ACCOUNT_UNLOCKED = "ACCOUNT_UNLOCKED";
    public static final String EVENT_MFA_ENABLED = "MFA_ENABLED";
    public static final String EVENT_MFA_DISABLED = "MFA_DISABLED";
    public static final String EVENT_TOKEN_REFRESH = "TOKEN_REFRESH";
    public static final String EVENT_REGISTRATION = "REGISTRATION";
}