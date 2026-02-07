package com.bmglewis.controller;

import com.bmglewis.dto.auth.*;
import com.bmglewis.dto.user.UserDto;
import com.bmglewis.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<UserDto> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request, httpRequest));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login and get authentication",
            description = "Returns user info. Access and refresh tokens are set as HttpOnly cookies for security."
    )
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        return ResponseEntity.ok(authService.login(request, httpRequest, httpResponse));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Reads refresh token from HttpOnly cookie, returns new tokens in cookies"
    )
    public ResponseEntity<AuthResponse> refreshToken(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        return ResponseEntity.ok(authService.refreshToken(httpRequest, httpResponse));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Logout and clear authentication",
            description = "Revokes refresh token and clears all auth cookies"
    )
    public ResponseEntity<MessageResponse> logout(
            Authentication authentication,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        return ResponseEntity.ok(authService.logout(
                authentication.getName(),
                httpRequest,
                httpResponse
        ));
    }

    @GetMapping("/profile")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get user profile")
    public ResponseEntity<UserDto> getProfile(Authentication authentication) {
        return ResponseEntity.ok(authService.getProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update user profile")
    public ResponseEntity<UserDto> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(authService.updateProfile(
                authentication.getName(),
                request,
                httpRequest
        ));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(authService.forgotPassword(request, httpRequest));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password with token",
            description = "Resets password and clears all auth cookies"
    )
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        return ResponseEntity.ok(authService.resetPassword(request, httpRequest, httpResponse));
    }

    @PostMapping("/change-password")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Change password",
            description = "Changes password and clears all auth cookies. User must login again."
    )
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        return ResponseEntity.ok(authService.changePassword(
                authentication.getName(),
                request,
                httpRequest,
                httpResponse
        ));
    }
}