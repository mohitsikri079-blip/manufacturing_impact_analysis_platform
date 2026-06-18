package com.miae.copilot.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data transfer object for chat requests sent to the Manufacturing Impact Copilot. 
 * <p>This DTO contains the user's message and a session ID to maintain context across multiple interactions.
 */
public record ChatRequest(
        @NotBlank String message,
        @NotBlank String sessionId
) {
}
