package com.miae.security;

import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * Strategy for handling different authentication mechanisms.
 */
public interface AuthenticationStrategy {
    AuthenticationType type();
    default JwtDecoder jwtDecoder() { throw new UnsupportedOperationException("This type does not use JWTs"); }
}
