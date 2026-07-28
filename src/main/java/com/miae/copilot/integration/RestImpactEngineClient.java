package com.miae.copilot.integration;

import com.miae.analysis.ImpactEntityType;
import com.miae.copilot.config.CopilotProperties;
import com.miae.copilot.dto.ImpactAnalysisPayload;
import com.miae.copilot.exception.CopilotException;
import com.miae.security.AuthenticationType;
import com.miae.security.SecurityProperties;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Implementation of the ImpactEngineClient interface that uses a REST client to communicate with the Impact Engine service. 
 * <p>This class constructs the appropriate request based on the entity type and ID, sends it to the Impact Engine's API endpoint, and processes the response to return a structured map of results.
  */
@Component
public class RestImpactEngineClient implements ImpactEngineClient {

    private final RestClient restClient;
    private final CopilotProperties properties;
    private final SecurityProperties securityProperties;

    public RestImpactEngineClient(RestClient.Builder builder, CopilotProperties properties, SecurityProperties securityProperties) {
        this.properties = properties;
        this.securityProperties = securityProperties;
        this.restClient = builder.baseUrl(properties.getImpactEngine().getBaseUrl()).build();
    }

    @Override
    public Map<String, Object> analyze(ImpactEntityType entityType, String entityId) {
        try {
            RestClient.RequestBodySpec request = restClient.post()
                    .uri("/api/v1/impact-analysis")
                    .contentType(MediaType.APPLICATION_JSON);
            if (securityProperties.getAuthenticationType() == AuthenticationType.DEVELOPER_API_KEY) {
                request.header(securityProperties.getApiKey().getHeaderName(), properties.getImpactEngine().getApiKey());
            } else {
                request.header("Authorization", bearerToken());
            }
            return request
                    .body(new ImpactAnalysisPayload(entityType, entityId))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException ex) {
            throw new CopilotException("Impact Engine analysis request failed", ex);
        }
    }

    private String bearerToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String authorization = attributes == null ? null : attributes.getRequest().getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new CopilotException("Bearer token is required for the Impact Engine request");
        }
        return authorization;
    }
}
