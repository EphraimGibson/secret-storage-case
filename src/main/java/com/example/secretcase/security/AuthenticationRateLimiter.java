package com.example.secretcase.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthenticationRateLimiter {
    private static final int MAX_FAILURES = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

    private final Map<String, FailedAttempts> usernameLockouts = new ConcurrentHashMap<>();

    private static class FailedAttempts {
        int count = 0;
        Instant lockedUntil;
    }

    boolean isUsernameLockedOut(String username) {
        return isLockedOut(usernameLockouts, username);
    }

    void recordUsernameFailure(String username) {
        recordFailure(usernameLockouts, username);
    }

    private boolean isLockedOut(
            Map<String, FailedAttempts> lockouts,
            String key) {

        FailedAttempts attempts = lockouts.get(key);

        if (attempts == null) {
            return false;
        }

        synchronized (attempts) {
            return attempts.lockedUntil != null
                    && Instant.now().isBefore(attempts.lockedUntil);
        }
    }

    private void recordFailure(
            Map<String, FailedAttempts> lockouts,
            String key) {
        FailedAttempts attempts = lockouts.computeIfAbsent(key, k -> new FailedAttempts());
        synchronized (attempts) {
            attempts.count++;
            if (attempts.count >= MAX_FAILURES) {
                attempts.lockedUntil = Instant.now().plus(LOCKOUT_DURATION);
            }
        }
    }
}