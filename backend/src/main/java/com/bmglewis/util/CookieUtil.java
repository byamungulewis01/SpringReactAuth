package com.bmglewis.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;


@Component
public class CookieUtil {

    @Value("${cookie.domain:localhost}")
    private String cookieDomain;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${cookie.access-token-name:access_token}")
    private String accessTokenCookieName;

    @Value("${cookie.refresh-token-name:refresh_token}")
    private String refreshTokenCookieName;

    @Value("${jwt.expiration:900000}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshTokenExpiration;

    /**
     * Create HttpOnly cookie for access token
     */
    public void createAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(accessTokenCookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure); // Set to true in production (HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge((int) (accessTokenExpiration / 1000)); // Convert to seconds

        // SameSite attribute for CSRF protection
        cookie.setAttribute("SameSite", "Strict");

        if (!cookieDomain.equals("localhost")) {
            cookie.setDomain(cookieDomain);
        }

        response.addCookie(cookie);
    }

    /**
     * Create HttpOnly cookie for refresh token
     */
    public void createRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(refreshTokenCookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure); // Set to true in production (HTTPS)
        cookie.setPath("/"); // Only send to refresh endpoint
        cookie.setMaxAge((int) (refreshTokenExpiration / 1000)); // Convert to seconds

        // SameSite attribute for CSRF protection
        cookie.setAttribute("SameSite", "Strict");

        if (!cookieDomain.equals("localhost")) {
            cookie.setDomain(cookieDomain);
        }

        response.addCookie(cookie);
    }

    /**
     * Get access token from cookie
     */
    public Optional<String> getAccessTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, accessTokenCookieName);
    }

    /**
     * Get refresh token from cookie
     */
    public Optional<String> getRefreshTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, refreshTokenCookieName);
    }

    /**
     * Delete access token cookie
     */
    public void deleteAccessTokenCookie(HttpServletResponse response) {
        deleteCookie(response, accessTokenCookieName, "/");
    }

    /**
     * Delete refresh token cookie
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        deleteCookie(response, refreshTokenCookieName, "/auth/refresh");
    }

    /**
     * Delete all auth cookies
     */
    public void deleteAllAuthCookies(HttpServletResponse response) {
        deleteAccessTokenCookie(response);
        deleteRefreshTokenCookie(response);
    }

    /**
     * Helper method to get cookie value
     */
    private Optional<String> getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> name.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst();
        }
        return Optional.empty();
    }

    /**
     * Helper method to delete cookie
     */
    private void deleteCookie(HttpServletResponse response, String name, String path) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath(path);
        cookie.setMaxAge(0); // Delete immediately

        if (!cookieDomain.equals("localhost")) {
            cookie.setDomain(cookieDomain);
        }

        response.addCookie(cookie);
    }
}