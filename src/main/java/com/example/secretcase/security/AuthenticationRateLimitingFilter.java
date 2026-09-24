package com.example.secretcase.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AuthenticationRateLimitingFilter extends OncePerRequestFilter {
    private final AuthenticationRateLimiter rateLimiter;

    public AuthenticationRateLimitingFilter(AuthenticationRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String username = usernameFrom(request);

        if (username != null && rateLimiter.isUsernameLockedOut(username)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("text/plain");
            response.getWriter().write("Too many failed attempts. Try again later.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String usernameFrom(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Basic ")) {
            return null;
        }

        try {
            String credentials = new String(
                    Base64.getDecoder().decode(authorization.substring(6)),
                    StandardCharsets.ISO_8859_1);
            int separator = credentials.indexOf(':');
            return separator >= 0 ? credentials.substring(0, separator) : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}