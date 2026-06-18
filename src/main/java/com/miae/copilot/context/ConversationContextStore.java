package com.miae.copilot.context;

import com.miae.analysis.ImpactEntityType;
import com.miae.copilot.dto.ConversationContext;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Component responsible for storing and managing conversation contexts for different user sessions in the 
 * Manufacturing Impact Copilot application. It allows for retrieval, updating, and resetting of conversation contexts based on session identifiers.
 */
@Component
public class ConversationContextStore {

    private final Map<String, ConversationContext> contexts = new ConcurrentHashMap<>();

    public Optional<ConversationContext> find(String sessionId) {
        return Optional.ofNullable(contexts.get(sessionId));
    }

    public void update(String sessionId, ImpactEntityType entityType, String entityId, Map<String, Object> impactResponse) {
        contexts.put(sessionId, new ConversationContext(entityType, entityId, impactResponse));
    }

    public void reset(String sessionId) {
        contexts.remove(sessionId);
    }
}
