package com.bmglewis.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> loginCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registerCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> passwordResetCache = new ConcurrentHashMap<>();

    // Login: 5 attempts per minute per IP
    private Bucket createLoginBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // Registration: 3 attempts per hour per IP
    private Bucket createRegisterBucket() {
        Bandwidth limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofHours(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // Password reset: 3 attempts per hour per IP
    private Bucket createPasswordResetBucket() {
        Bandwidth limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofHours(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Only apply rate limiting to specific endpoints
        if ("POST".equals(method)) {
            if (path.contains("/auth/login")) {
                if (!checkRateLimit(request, response, loginCache, this::createLoginBucket, "login")) {
                    return;
                }
            } else if (path.contains("/auth/register")) {
                if (!checkRateLimit(request, response, registerCache, this::createRegisterBucket, "registration")) {
                    return;
                }
            }
            else if (path.contains("/auth/forgot-password") || path.contains("/auth/reset-password")) {
                if (!checkRateLimit(request, response, passwordResetCache, this::createPasswordResetBucket, "password reset")) {
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean checkRateLimit(
            HttpServletRequest request,
            HttpServletResponse response,
            Map<String, Bucket> cache,
            BucketSupplier bucketSupplier,
            String endpointName
    ) throws IOException {
        String clientIp = getClientIP(request);
        Bucket bucket = cache.computeIfAbsent(clientIp, k -> bucketSupplier.get());

        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for {} from IP: {}", endpointName, clientIp);

            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write(String.format("""
                {
                    "timestamp": "%s",
                    "status": 429,
                    "error": "Too Many Requests",
                    "message": "Rate limit exceeded for %s. Please try again later.",
                    "path": "%s"
                }
                """,
                    java.time.LocalDateTime.now(),
                    endpointName,
                    request.getRequestURI()
            ));
            return false;
        }

        return true;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        // Get first IP in case of multiple proxies
        return xfHeader.split(",")[0].trim();
    }

    @FunctionalInterface
    private interface BucketSupplier {
        Bucket get();
    }
}