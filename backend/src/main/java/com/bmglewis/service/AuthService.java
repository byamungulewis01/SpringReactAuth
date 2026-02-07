package com.bmglewis.service;

import com.bmglewis.dto.auth.*;
import com.bmglewis.dto.user.UserDto;
import com.bmglewis.exception.*;
import com.bmglewis.model.PasswordResetToken;
import com.bmglewis.model.RefreshToken;
import com.bmglewis.model.Role;
import com.bmglewis.model.SecurityAuditLog;
import com.bmglewis.model.User;
import com.bmglewis.repository.PasswordResetTokenRepository;
import com.bmglewis.repository.RoleRepository;
import com.bmglewis.repository.UserRepository;
import com.bmglewis.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final UserService userService;
    private final SecurityAuditService auditService;
    private final AccountLockService accountLockService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;

    @Transactional
    public UserDto register(RegisterRequest request, HttpServletRequest httpRequest) {
        log.info("Registering user: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            auditService.logSecurityEvent(
                    request.email(),
                    SecurityAuditLog.EVENT_REGISTRATION,
                    httpRequest,
                    false,
                    "Email already exists"
            );
            throw new UserAlreadyExistsException("Email already registered");
        }

        // Find default role
        Role defaultRole = roleRepository.findByRoleName("Requester")
                .orElseThrow(() -> new ResourceNotFoundException("Default role 'Requester' not found"));

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .isActive(true)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .roles(new HashSet<>(Set.of(defaultRole)))
                .build();

        User savedUser = userRepository.save(user);

        auditService.logSecurityEvent(
                savedUser.getEmail(),
                SecurityAuditLog.EVENT_REGISTRATION,
                httpRequest,
                true,
                null
        );

        log.info("User registered successfully: {}", savedUser.getEmail());
        return mapToDto(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        log.info("Login attempt: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    auditService.logSecurityEvent(
                            request.email(),
                            SecurityAuditLog.EVENT_FAILED_LOGIN,
                            httpRequest,
                            false,
                            "User not found"
                    );
                    return new InvalidCredentialsException("Invalid credentials");
                });

        // Check if account is locked
        try {
            accountLockService.checkAccountLock(user);
        } catch (AccountLockedException e) {
            auditService.logSecurityEvent(
                    request.email(),
                    SecurityAuditLog.EVENT_FAILED_LOGIN,
                    httpRequest,
                    false,
                    "Account locked"
            );
            throw e;
        }

        // Authenticate user
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            accountLockService.recordFailedLogin(user, httpRequest);
            auditService.logSecurityEvent(
                    request.email(),
                    SecurityAuditLog.EVENT_FAILED_LOGIN,
                    httpRequest,
                    false,
                    "Invalid credentials"
            );
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Successful login
        accountLockService.recordSuccessfulLogin(user);
        user.setLastLoginIp(getClientIP(httpRequest));
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, httpRequest);

        // Set tokens in HttpOnly cookies
        cookieUtil.createAccessTokenCookie(httpResponse, accessToken);
        cookieUtil.createRefreshTokenCookie(httpResponse, refreshToken.getToken());

        auditService.logSecurityEvent(
                user.getEmail(),
                SecurityAuditLog.EVENT_LOGIN,
                httpRequest,
                true,
                null
        );

        log.info("User logged in successfully: {}", user.getEmail());

        // Return user info (tokens are in cookies)
        return new AuthResponse(
                mapToDto(user),
                jwtService.getAccessTokenExpiration() / 1000
        );
    }

    @Transactional
    public AuthResponse refreshToken(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        log.info("Refresh token request");

        // Get refresh token from cookie
        String refreshTokenValue = cookieUtil.getRefreshTokenFromCookie(httpRequest)
                .orElseThrow(() -> new TokenException("Refresh token not found in cookies"));

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenValue);
        User user = refreshToken.getUser();

        // Rotate refresh token
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken, httpRequest);

        // Generate new access token
        String newAccessToken = jwtService.generateToken(user);

        // Update cookies
        cookieUtil.createAccessTokenCookie(httpResponse, newAccessToken);
        cookieUtil.createRefreshTokenCookie(httpResponse, newRefreshToken.getToken());

        auditService.logSecurityEvent(
                user.getEmail(),
                SecurityAuditLog.EVENT_TOKEN_REFRESH,
                httpRequest,
                true,
                null
        );

        log.info("Token refreshed for user: {}", user.getEmail());

        return new AuthResponse(
                mapToDto(user),
                jwtService.getAccessTokenExpiration() / 1000
        );
    }

    @Transactional
    public MessageResponse logout(String email, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        log.info("Logout request for user: {}", email);

        // Get refresh token from cookie and revoke it
        cookieUtil.getRefreshTokenFromCookie(httpRequest).ifPresent(token -> {
            try {
                refreshTokenService.revokeToken(token);
            } catch (Exception e) {
                log.warn("Failed to revoke refresh token: {}", e.getMessage());
            }
        });

        // Delete all auth cookies
        cookieUtil.deleteAllAuthCookies(httpResponse);

        auditService.logSecurityEvent(
                email,
                SecurityAuditLog.EVENT_LOGOUT,
                httpRequest,
                true,
                null
        );

        return new MessageResponse("Logged out successfully", true);
    }

    @Transactional(readOnly = true)
    public UserDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDto(user);
    }

    @Transactional
    public UserDto updateProfile(String email, UpdateProfileRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean updated = false;

        if (request.firstName() != null && !request.firstName().equals(user.getFirstName())) {
            user.setFirstName(request.firstName());
            updated = true;
        }
        if (request.lastName() != null && !request.lastName().equals(user.getLastName())) {
            user.setLastName(request.lastName());
            updated = true;
        }
        if (request.phone() != null && !request.phone().equals(user.getPhone())) {
            user.setPhone(request.phone());
            updated = true;
        }

        if (updated) {
            User updatedUser = userRepository.save(user);
            auditService.logSecurityEvent(
                    email,
                    "PROFILE_UPDATE",
                    httpRequest,
                    true,
                    null
            );
            return mapToDto(updatedUser);
        }

        return mapToDto(user);
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        log.info("Password reset request for: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    // Don't reveal if email exists
                    return new ResourceNotFoundException("If the email exists, a reset link will be sent");
                });

        // Check rate limiting (max 3 requests per hour)
        long recentTokens = tokenRepository.countRecentTokensByEmail(
                request.email(),
                LocalDateTime.now().minusHours(1)
        );
        if (recentTokens >= 3) {
            auditService.logSecurityEvent(
                    request.email(),
                    SecurityAuditLog.EVENT_PASSWORD_RESET,
                    httpRequest,
                    false,
                    "Too many reset requests"
            );
            throw new RateLimitExceededException("Too many password reset requests. Please try again later.");
        }

        // Delete old tokens
        tokenRepository.deleteByUser(user);

        // Generate cryptographically secure token
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        String tokenValue = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        // Hash the token before storing
        String hashedToken = passwordEncoder.encode(tokenValue);

        PasswordResetToken token = PasswordResetToken.builder()
                .token(hashedToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15)) // 15 minutes
                .used(false)
                .ipAddress(getClientIP(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .build();

        tokenRepository.save(token);


        // Send email with unhashed token and email
        String userName = user.getFirstName() != null ? user.getFirstName() : user.getEmail();
        emailService.sendPasswordResetEmail(user.getEmail(), userName, tokenValue, user.getEmail());


        auditService.logSecurityEvent(
                request.email(),
                SecurityAuditLog.EVENT_PASSWORD_RESET,
                httpRequest,
                true,
                null,
                "Reset token generated"
        );

        return new MessageResponse("Password reset email sent", true);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        log.info("Password reset for: {}", request.email());

        // Find all valid tokens for this email
        List<PasswordResetToken> validTokens = tokenRepository.findValidTokensByEmail(
                request.email(),
                LocalDateTime.now()
        );

        // Find matching token by comparing hashes
        PasswordResetToken matchingToken = validTokens.stream()
                .filter(token -> passwordEncoder.matches(request.token(), token.getToken()))
                .filter(token -> token.getIpAddress().equals(getClientIP(httpRequest)))
                .findFirst()
                .orElseThrow(() -> {
                    auditService.logSecurityEvent(
                            request.email(),
                            SecurityAuditLog.EVENT_PASSWORD_RESET,
                            httpRequest,
                            false,
                            "Invalid or expired token"
                    );
                    return new TokenException("Invalid or expired reset token");
                });

        User user = matchingToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        matchingToken.markAsUsed();
        tokenRepository.save(matchingToken);

        // Revoke all refresh tokens
        refreshTokenService.revokeAllUserTokens(user, httpRequest);

        // Clear cookies
        cookieUtil.deleteAllAuthCookies(httpResponse);

        auditService.logSecurityEvent(
                request.email(),
                SecurityAuditLog.EVENT_PASSWORD_RESET,
                httpRequest,
                true,
                null,
                "Password reset successful"
        );

        return new MessageResponse("Password reset successful. Please login with your new password.", true);
    }

    @Transactional
    public MessageResponse changePassword(String email, ChangePasswordRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            auditService.logSecurityEvent(
                    email,
                    SecurityAuditLog.EVENT_PASSWORD_CHANGE,
                    httpRequest,
                    false,
                    "Current password incorrect"
            );
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Revoke all refresh tokens
        refreshTokenService.revokeAllUserTokens(user, httpRequest);

        // Clear cookies
        cookieUtil.deleteAllAuthCookies(httpResponse);

        auditService.logSecurityEvent(
                email,
                SecurityAuditLog.EVENT_PASSWORD_CHANGE,
                httpRequest,
                true,
                null
        );

        return new MessageResponse("Password changed successfully. Please login again.", true);
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getDepartment() != null ? user.getDepartment().getDepartmentId() : null,
                user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null,
                user.getManager() != null ? user.getManager().getUserId() : null,
                user.getManager() != null ? user.getManager().getFullName() : null,
                user.getIsActive(),
                user.getLastLoginAt(),
                user.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet()),
                user.getCreatedAt()
        );
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}