package com.bmglewis.security;

import com.bmglewis.service.JwtService;
import com.bmglewis.util.CookieUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * FILE TYPE: CLASS (Filter)
 * Enhanced JWT authentication filter with HttpOnly cookie support
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip JWT validation for public endpoints
        String path = request.getRequestURI();
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = null;

        // Try to get JWT from Authorization header first (for API clients)
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        } else {
            // If not in header, try to get from HttpOnly cookie (for browser clients)
            jwt = cookieUtil.getAccessTokenFromCookie(request).orElse(null);
        }

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("User authenticated: {}", userEmail);
                } else {
                    log.warn("Invalid token for user: {}", userEmail);
                }
            }
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
            handleJwtException(response, "JWT token has expired", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            handleJwtException(response, "Malformed JWT token", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            handleJwtException(response, "Invalid JWT signature", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (IllegalArgumentException e) {
            log.warn("JWT token compact of handler are invalid: {}", e.getMessage());
            handleJwtException(response, "Invalid JWT token", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            handleJwtException(response, "Authentication error", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleJwtException(HttpServletResponse response, String message, int status)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(String.format("""
            {
                "timestamp": "%s",
                "status": %d,
                "error": "Unauthorized",
                "message": "%s"
            }
            """,
                java.time.LocalDateTime.now(),
                status,
                message
        ));
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/auth/register") ||
                path.startsWith("/auth/login") ||
                path.startsWith("/auth/forgot-password") ||
                path.startsWith("/auth/reset-password") ||
                path.startsWith("/auth/refresh") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/actuator/health");
    }
}