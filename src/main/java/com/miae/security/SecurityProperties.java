package com.miae.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 
 * SecurityProperties binds all miae.security.* configuration from application.yml
 */
@ConfigurationProperties("miae.security")
public class SecurityProperties {
    private AuthenticationType authenticationType = AuthenticationType.DEVELOPER_API_KEY;
    private ApiKey apiKey = new ApiKey();
    private PublicKey publicKey = new PublicKey();
    private Jwks jwks = new Jwks();
    private String issuer;
    private String audience;
    private Duration clockSkew = Duration.ofSeconds(60);
    public AuthenticationType getAuthenticationType() { return authenticationType; }
    public void setAuthenticationType(AuthenticationType value) { authenticationType = value; }
    public ApiKey getApiKey() { return apiKey; }
    public void setApiKey(ApiKey value) { apiKey = value; }
    public PublicKey getPublicKey() { return publicKey; }
    public void setPublicKey(PublicKey value) { publicKey = value; }
    public Jwks getJwks() { return jwks; }
    public void setJwks(Jwks value) { jwks = value; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String value) { issuer = value; }
    public String getAudience() { return audience; }
    public void setAudience(String value) { audience = value; }
    public Duration getClockSkew() { return clockSkew; }
    public void setClockSkew(Duration value) { clockSkew = value; }
    public static class ApiKey {
        private String headerName = "X-API-Key";
        private String value;
        public String getHeaderName() { return headerName; }
        public void setHeaderName(String headerName) { this.headerName = headerName; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
    public static class PublicKey {
        private String location;
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
    public static class Jwks {
        private String uri;
        public String getUri() { return uri; }
        public void setUri(String uri) { this.uri = uri; }
    }
}
