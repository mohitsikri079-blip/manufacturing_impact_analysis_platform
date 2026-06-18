package com.miae.copilot.dto;

import com.miae.analysis.ImpactEntityType;
import java.util.Map;

/**
 * Data transfer object for maintaining the context of a conversation with the Manufacturing Impact Copilot.
 * <p>This DTO contains the type and ID of the last entity that was analyzed, as well as the last impact response received from the Impact Engine. 
 * This context is used to provide continuity in the conversation, allowing the Copilot to reference previous analyses and provide more relevant responses to user queries.
 */
public record ConversationContext(
        ImpactEntityType lastEntityType,
        String lastEntityId,
        Map<String, Object> lastImpactResponse
) {
}
