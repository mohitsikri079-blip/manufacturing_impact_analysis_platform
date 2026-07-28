package com.miae.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 
 * AuthenticationStrategyFactory
 */
@Component
public class AuthenticationStrategyFactory {
    private final Map<AuthenticationType, AuthenticationStrategy> strategies = new HashMap<>();
    private final SecurityProperties properties;
    public AuthenticationStrategyFactory(List<AuthenticationStrategy> candidates, SecurityProperties properties) {
        candidates.forEach(candidate -> strategies.put(candidate.type(), candidate));
        this.properties = properties;
    }
    public AuthenticationStrategy selected() {
        AuthenticationStrategy strategy = strategies.get(properties.getAuthenticationType());
        if (strategy == null) throw new IllegalStateException("Unsupported authentication type: " + properties.getAuthenticationType());
        return strategy;
    }
}
