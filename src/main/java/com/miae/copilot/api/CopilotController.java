package com.miae.copilot.api;

import com.miae.copilot.dto.ChatRequest;
import com.miae.copilot.dto.ChatResponse;
import com.miae.copilot.service.CopilotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling chat requests to the Manufacturing Impact Copilot. This controller defines an endpoint for receiving user messages along with the intent and context of the message, and it delegates the processing of these messages to the CopilotService. 
 * <p>The service will perform impact analysis based on the provided context and generate a business-friendly response using the OpenAI API, which is then returned to the user in a ChatResponse object.
 * <p>The chat endpoint expects a ChatRequest object that contains the user's message, the intent of the message (e.g., "impact_analysis"), the type and ID of the entity being analyzed (e.g., "product" and "P123"), and any additional context needed for the analysis. 
 * <p>The controller ensures that the request is valid before passing it to the service layer, and it handles any exceptions that may arise during the processing of the request, such as invalid input or issues with the OpenAI API. The response from the service is structured to provide a clear and concise answer to the user's question, along with relevant impact metrics and supporting evidence based on the analysis performed.   
 */
@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Copilot", description = "Natural-language manufacturing impact analysis backed by the MIAE impact engine.")
public class CopilotController {

    private final CopilotService copilotService;

    public CopilotController(CopilotService copilotService) {
        this.copilotService = copilotService;
    }

    @PostMapping
    @Operation(summary = "Send a Copilot message", description = "Interprets a manufacturing-impact question and returns a contextual chat response.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Copilot response generated."),
            @ApiResponse(responseCode = "400", description = "The request is invalid."),
            @ApiResponse(responseCode = "401", description = "Authentication is missing or invalid."),
            @ApiResponse(responseCode = "503", description = "Copilot is unavailable or not configured.")
    })
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return copilotService.chat(request);
    }
}
