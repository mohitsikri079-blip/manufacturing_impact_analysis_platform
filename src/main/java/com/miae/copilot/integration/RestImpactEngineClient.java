package com.miae.copilot.integration;

import com.miae.analysis.ImpactEntityType;
import com.miae.copilot.config.CopilotProperties;
import com.miae.copilot.dto.ImpactAnalysisPayload;
import com.miae.copilot.exception.CopilotException;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Implementation of the ImpactEngineClient interface that uses a REST client to communicate with the Impact Engine service. 
 * <p>This class constructs the appropriate request based on the entity type and ID, sends it to the Impact Engine's API endpoint, and processes the response to return a structured map of results.
  */
@Component
public class RestImpactEngineClient implements ImpactEngineClient {

    private final RestClient restClient;
    private final CopilotProperties properties;

    public RestImpactEngineClient(RestClient.Builder builder, CopilotProperties properties) {
        this.properties = properties;
        this.restClient = builder.baseUrl(properties.getImpactEngine().getBaseUrl()).build();
    }

    @Override
    public Map<String, Object> analyze(ImpactEntityType entityType, String entityId) {
        try {
            return restClient.post()
                    .uri("/api/v1/impact-analysis")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-API-Key", properties.getImpactEngine().getApiKey())
                    .body(new ImpactAnalysisPayload(entityType, entityId))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException ex) {
            throw new CopilotException("Impact Engine analysis request failed", ex);
        }
    }
}
