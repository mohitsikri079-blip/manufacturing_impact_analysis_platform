package com.miae.security;

import org.springframework.stereotype.Component;

@Component
public class DeveloperApiKeyAuthenticationStrategy implements AuthenticationStrategy {
    @Override public AuthenticationType type() { return AuthenticationType.DEVELOPER_API_KEY; }
}
