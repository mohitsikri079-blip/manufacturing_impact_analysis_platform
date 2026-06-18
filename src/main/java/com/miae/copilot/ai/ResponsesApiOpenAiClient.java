package com.miae.copilot.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miae.copilot.config.CopilotProperties;
import com.miae.copilot.exception.CopilotConfigurationException;
import com.miae.copilot.exception.CopilotException;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Implementation of the OpenAiClient interface that uses a REST client to communicate with the OpenAI API. This class constructs the appropriate request body based on the user message, intent, entity information, and impact analysis results, and sends it to the OpenAI API's /responses endpoint. The response from the API is then processed to extract the generated business response text, which is returned to the caller.
 * <p>The system prompt is designed to instruct the OpenAI model to act as a Manufacturing Impact Copilot, providing concise, evidence-based answers to user questions about manufacturing impacts. The client also handles configuration checks to ensure that the Copilot is enabled and that the necessary API key is provided, throwing exceptions if the configuration is invalid.
 * If the OpenAI API call fails or returns an unexpected response, a CopilotException is thrown to indicate that the response generation process failed.
 */
@Component
public class ResponsesApiOpenAiClient implements OpenAiClient {

    private static final String SYSTEM_PROMPT = """
            You are Manufacturing Impact Copilot for product managers and manufacturing leaders.
            Use only the supplied Impact Engine JSON as evidence.
            Do not invent manufacturing facts, graph relationships, recommendations, approvals, or ERP actions.
            Produce a concise human-readable answer with:
            Executive Summary
            Impact Metrics
            Supporting Evidence
            If evidence is missing, say what is missing.
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final CopilotProperties properties;

    public ResponsesApiOpenAiClient(RestClient.Builder builder, ObjectMapper objectMapper, CopilotProperties properties) {
        this.restClient = builder.baseUrl(properties.getOpenai().getBaseUrl()).build();
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public String generateBusinessResponse(String userMessage, String intent, String entityType, String entityId, String impactJson) {
        if (!properties.isEnabled()) {
            return "Manufacturing Impact Copilot is disabled. Set COPILOT_ENABLED=true and configure OPENAI_API_KEY.";
        }
        if (isBlank(properties.getOpenai().getApiKey())) {
            throw new CopilotConfigurationException("OPENAI_API_KEY is required when Copilot is enabled");
        }

        Map<String, Object> body = Map.of(
                "model", properties.getOpenai().getModel(),
                "input", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userPrompt(userMessage, intent, entityType, entityId, impactJson))
                )
        );

        try {
            JsonNode response = restClient.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getOpenai().getApiKey())
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            return extractText(response);
        } catch (RestClientException ex) {
            throw new CopilotException("OpenAI response generation failed", ex);
        }
    }

    private String userPrompt(String userMessage, String intent, String entityType, String entityId, String impactJson) {
        return """
                User question:
                %s

                Detected intent:
                %s

                Active manufacturing entity:
                %s / %s

                Impact Engine JSON:
                %s
                """.formatted(userMessage, intent, entityType, entityId, impactJson);
    }

    private String extractText(JsonNode response) {
        if (response == null || response.isMissingNode() || response.isNull()) {
            throw new CopilotException("OpenAI returned an empty response");
        }
        JsonNode outputText = response.path("output_text");
        if (outputText.isTextual() && !outputText.asText().isBlank()) {
            return outputText.asText();
        }
        JsonNode output = response.path("output");
        if (output.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : output) {
                JsonNode content = item.path("content");
                if (!content.isArray()) {
                    continue;
                }
                for (JsonNode contentItem : content) {
                    if ("output_text".equals(contentItem.path("type").asText())) {
                        builder.append(contentItem.path("text").asText()).append(System.lineSeparator());
                    }
                }
            }
            String text = builder.toString().trim();
            if (!text.isBlank()) {
                return text;
            }
        }
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception ex) {
            return response.toString();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
