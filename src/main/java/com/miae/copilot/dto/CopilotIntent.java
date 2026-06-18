package com.miae.copilot.dto;

import com.miae.analysis.ImpactEntityType;

/**
 * Data transfer object representing the intent of a user's message in the context of the Manufacturing Impact Copilot.
 * <p>This DTO contains the category of the intent (e.g., impact analysis, clarification), the type and ID of the entity being analyzed, and flags indicating whether further clarification is required along with a message for clarification if needed. 
 * <p>The CopilotService uses this DTO to determine how to process the user's message, whether to perform an impact analysis, request additional information, or provide a response based on the existing context and analysis results.
 */
public record CopilotIntent(
        IntentCategory category,
        ImpactEntityType entityType,
        String entityId,
        boolean requiresClarification,
        String clarificationMessage
) {

    public static CopilotIntent clarification(String message) {
        return new CopilotIntent(IntentCategory.CLARIFICATION, null, null, true, message);
    }
}
