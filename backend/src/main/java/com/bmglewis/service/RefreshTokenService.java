package com.bmglewis.service;

import com.bmglewis.exception.TokenException;
import com.bmglewis.model.RefreshToken;
import com.bmglewis.model.User;
import com.bmglewis.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityAuditService auditService;

    @Value("${jwt.refresh-expiration:604800000}") // 7 days default
    private Long refreshTokenExpiration;

    @Value("${security.max-refresh-tokens-per-user:5}")
    private int maxRefreshTokensPerUser;

    /**
     * Generate a new refresh token
     */
    @Transactional
    public RefreshToken createRefreshToken(User user, HttpServletRequest request) {
        // Clean up old tokens if user has too many
        cleanupOldTokens(user);

        // Generate cryptographically secure token
        String tokenValue = generateSecureToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .ipAddress(getClientIP(request))
                .userAgent(request.getHeader("User-Agent"))
                .revoked(false)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token created for user: {}", user.getEmail());
        return saved;
    }

    /**
     * Verify and get refresh token
     */
    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Invalid refresh token"));

        if (refreshToken.getRevoked()) {
            throw new TokenException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            throw new TokenException("Refresh token has expired");
        }

        return refreshToken;
    }

    /**
     * Rotate refresh token (revoke old, create new)
     */
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken, HttpServletRequest request) {
        // Revoke old token
        oldToken.revoke();
        refreshTokenRepository.save(oldToken);

        // Create new token
        RefreshToken newToken = createRefreshToken(oldToken.getUser(), request);

        log.info("Refresh token rotated for user: {}", oldToken.getUser().getEmail());
        return newToken;
    }

    /**
     * Revoke all refresh tokens for a user
     */
    @Transactional
    public void revokeAllUserTokens(User user, HttpServletRequest request) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUser(user);
        tokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(tokens);

        auditService.logSecurityEvent(
                user.getEmail(),
                "TOKEN_REVOCATION",
                request,
                true,
                null,
                "All refresh tokens revoked"
        );

        log.info("All refresh tokens revoked for user: {}", user.getEmail());
    }

    /**
     * Revoke specific refresh token
     */
    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Invalid refresh token"));

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        log.info("Refresh token revoked for user: {}", refreshToken.getUser().getEmail());
    }

    /**
     * Clean up old tokens if user exceeds max limit
     */
    private void cleanupOldTokens(User user) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findActiveTokensByUserId(
                user.getUserId(),
                LocalDateTime.now()
        );

        if (activeTokens.size() >= maxRefreshTokensPerUser) {
            // Revoke oldest tokens
            activeTokens.stream()
                    .sorted((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                    .limit(activeTokens.size() - maxRefreshTokensPerUser + 1)
                    .forEach(token -> {
                        token.revoke();
                        refreshTokenRepository.save(token);
                    });

            log.info("Cleaned up old refresh tokens for user: {}", user.getEmail());
        }
    }

    /**
     * Scheduled cleanup of expired and revoked tokens
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        refreshTokenRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
        log.info("Expired refresh tokens cleanup completed");
    }

    /**
     * Generate cryptographically secure random token
     */
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Get client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}