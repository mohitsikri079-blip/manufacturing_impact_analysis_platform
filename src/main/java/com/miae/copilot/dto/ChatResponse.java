package com.miae.copilot.dto;

import com.miae.analysis.ImpactEntityType;
/**
 * Data transfer object for chat responses returned by the Manufacturing Impact Copilot. 
 * <p>This DTO contains the generated answer to the user's query, the session ID to maintain context across multiple interactions, the type and ID of the entity being analyzed, and a flag indicating whether further clarification is required from the user.
 */
public record ChatResponse(
        String answer,
        String sessionId,
        ImpactEntityType entityType,
        String entityId,
        boolean clarificationRequired
) {
}
