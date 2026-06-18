package com.miae.copilot.ai;

/**
 * Interface for interacting with the OpenAI API to generate business responses based on user messages and impact analysis results. This client defines a method for generating a response that takes into account the user's message, the intent of the message, the type and ID of the entity being analyzed, and the JSON representation of the impact analysis results.
 * <p>Implementations of this interface will handle the actual communication with the OpenAI API, including constructing the prompt, sending the request, and processing the response to return a business-friendly message that can be presented to the user. This allows for a clean separation of concerns, where the service layer can focus on business logic and delegate the AI response generation to this client.
 */
public interface OpenAiClient {

    String generateBusinessResponse(String userMessage, String intent, String entityType, String entityId, String impactJson);
}
