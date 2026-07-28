package com.miae.security;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

/**
 * 
 * JwksAuthenticationStrategy is an implementation that uses JWKS (JSON Web Key Set) for JWT validation.
 */
@Component
public class JwksAuthenticationStrategy implements AuthenticationStrategy {
    private final SecurityProperties properties;
    public JwksAuthenticationStrategy(SecurityProperties properties) { this.properties = properties; }
    @Override public AuthenticationType type() { return AuthenticationType.JWKS; }
    @Override public JwtDecoder jwtDecoder() {
        String uri = properties.getJwks().getUri();
        if (uri == null || uri.isBlank()) throw new IllegalStateException("miae.security.jwks.uri is required for JWKS");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(uri).build();
        decoder.setJwtValidator(JwtValidationSupport.validator(properties));
        return decoder;
    }
}
