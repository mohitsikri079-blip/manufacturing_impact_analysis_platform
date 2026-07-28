package com.miae.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

/**
 * 
 * JwtPublicKeyAuthenticationStrategy is an implementation that uses a public key for JWT validation.
 */
@Component
public class JwtPublicKeyAuthenticationStrategy implements AuthenticationStrategy {
    private final SecurityProperties properties;
    private final ResourceLoader resourceLoader;
    public JwtPublicKeyAuthenticationStrategy(SecurityProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }
    @Override public AuthenticationType type() { return AuthenticationType.JWT_PUBLIC_KEY; }
    @Override public JwtDecoder jwtDecoder() {
        String location = properties.getPublicKey().getLocation();
        if (location == null || location.isBlank()) throw new IllegalStateException("miae.security.public-key.location is required for JWT_PUBLIC_KEY");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(loadPublicKey(location)).build();
        decoder.setJwtValidator(JwtValidationSupport.validator(properties));
        return decoder;
    }
    private RSAPublicKey loadPublicKey(String location) {
        try {
            Resource resource = resourceLoader.getResource(location);
            String pem = new String(resource.getInputStream().readAllBytes())
                    .replaceAll("-----BEGIN (?:RSA )?PUBLIC KEY-----", "")
                    .replaceAll("-----END (?:RSA )?PUBLIC KEY-----", "").replaceAll("\\s", "");
            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pem)));
        } catch (IOException | GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalStateException("Unable to load RSA public key from " + location, ex);
        }
    }
}
