package com.miae.security;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;

/**
 * 
 * JwtValidationSupport provides support for validating JWTs based on the configured security properties.
 */
final class JwtValidationSupport {
    private JwtValidationSupport() { }
    static OAuth2TokenValidator<Jwt> validator(SecurityProperties properties) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator(properties.getClockSkew()));
        if (hasText(properties.getIssuer())) validators.add(jwt -> properties.getIssuer().equals(jwt.getIssuer() == null ? null : jwt.getIssuer().toString())
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "JWT issuer is invalid", null)));
        if (hasText(properties.getAudience())) validators.add(jwt -> jwt.getAudience().contains(properties.getAudience())
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "JWT audience is invalid", null)));
        return new DelegatingOAuth2TokenValidator<>(validators);
    }
    private static boolean hasText(String value) { return value != null && !value.isBlank(); }
}
