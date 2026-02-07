package com.bmglewis.dto.auth;

public record AuthResponse(
        String accessToken,  // Optional - can be null if using cookies only
        String tokenType,
        Long expiresIn,
        com.bmglewis.dto.user.UserDto user
) {
    public AuthResponse(com.bmglewis.dto.user.UserDto user, Long expiresIn) {
        this(null, "Bearer", expiresIn, user);
    }
}