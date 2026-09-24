package com.example.secretcase.security;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureListener
        implements ApplicationListener<AbstractAuthenticationFailureEvent> {

    private final AuthenticationRateLimiter rateLimiter;

    public AuthenticationFailureListener(AuthenticationRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        rateLimiter.recordUsernameFailure(username);
    }
}