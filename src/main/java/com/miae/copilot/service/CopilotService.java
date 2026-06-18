package com.miae.copilot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miae.copilot.ai.OpenAiClient;
import com.miae.copilot.context.ConversationContextStore;
import com.miae.copilot.dto.ChatRequest;
import com.miae.copilot.dto.ChatResponse;
import com.miae.copilot.dto.ConversationContext;
import com.miae.copilot.dto.CopilotIntent;
import com.miae.copilot.integration.ImpactEngineClient;
import com.miae.copilot.intent.IntentDetector;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for handling the core logic of the Manufacturing Impact Copilot. 
 * <p>This service processes incoming chat requests, detects user intent, manages conversation context, invokes the Impact Engine for analysis, and generates responses using the OpenAI API.
 */
@Service
public class CopilotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CopilotService.class);

    private final IntentDetector intentDetector;
    private final ConversationContextStore contextStore;
    private final ImpactEngineClient impactEngineClient;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    public CopilotService(IntentDetector intentDetector,
                          ConversationContextStore contextStore,
                          ImpactEngineClient impactEngineClient,
                          OpenAiClient openAiClient,
                          ObjectMapper objectMapper) {
        this.intentDetector = intentDetector;
        this.contextStore = contextStore;
        this.impactEngineClient = impactEngineClient;
        this.openAiClient = openAiClient;
        this.objectMapper = objectMapper;
    }

    public ChatResponse chat(ChatRequest request) {
        Optional<ConversationContext> context = contextStore.find(request.sessionId());
        CopilotIntent intent = intentDetector.detect(request.message(), context);
        if (intent.requiresClarification()) {
            if (request.message().equalsIgnoreCase("reset") || request.message().equalsIgnoreCase("reset context")) {
                contextStore.reset(request.sessionId());
            }
            return new ChatResponse(intent.clarificationMessage(), request.sessionId(), null, null, true);
        }

        Map<String, Object> impactResponse = shouldReuseContextImpact(context, intent)
                ? context.get().lastImpactResponse()
                : impactEngineClient.analyze(intent.entityType(), intent.entityId());
        contextStore.update(request.sessionId(), intent.entityType(), intent.entityId(), impactResponse);

        String impactJson = toJson(impactResponse);
        LOGGER.info("Copilot query received. sessionId={}, intent={}, entityType={}, entityId={}",
                request.sessionId(), intent.category(), intent.entityType(), intent.entityId());
        String answer = openAiClient.generateBusinessResponse(
                request.message(),
                intent.category().name(),
                intent.entityType().name(),
                intent.entityId(),
                impactJson);

        return new ChatResponse(answer, request.sessionId(), intent.entityType(), intent.entityId(), false);
    }

    private boolean shouldReuseContextImpact(Optional<ConversationContext> context, CopilotIntent intent) {
        return context.isPresent()
                && context.get().lastImpactResponse() != null
                && context.get().lastEntityType() == intent.entityType()
                && context.get().lastEntityId().equals(intent.entityId());
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return value.toString();
        }
    }
}
