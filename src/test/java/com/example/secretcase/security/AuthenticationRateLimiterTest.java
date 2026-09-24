package com.example.secretcase.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationRateLimiterTest {
    private static final String TEST_USERNAME = "test-user";
    private static final String OTHER_TEST_USERNAME = "another-test-user";

    private final AuthenticationRateLimiter rateLimiter =
            new AuthenticationRateLimiter();

    @Test
    void testShouldLockUsernameAfterFiveFailures() {
        for (int i = 0; i < 4; i++) {
            rateLimiter.recordUsernameFailure(TEST_USERNAME);
            assertFalse(rateLimiter.isUsernameLockedOut(TEST_USERNAME));
        }

        rateLimiter.recordUsernameFailure(TEST_USERNAME);

        assertTrue(rateLimiter.isUsernameLockedOut(TEST_USERNAME));
    }

    @Test
    void testShouldTrackUsernamesIndependently() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordUsernameFailure(TEST_USERNAME);
        }

        assertTrue(rateLimiter.isUsernameLockedOut(TEST_USERNAME));
        assertFalse(rateLimiter.isUsernameLockedOut(OTHER_TEST_USERNAME));
    }
}