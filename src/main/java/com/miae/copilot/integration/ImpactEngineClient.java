package com.miae.copilot.integration;

import com.miae.analysis.ImpactEntityType;
import java.util.Map;

/**
 * Client interface for invoking the Impact Engine service to perform impact analysis based on entity type and ID. 
 * <p>This client defines a method for sending analysis requests to the Impact Engine and receiving the results as a map of key-value pairs, which can then be used by the CopilotService to generate responses for the user.
 * <p>Implementations of this interface will handle the actual communication with the Impact Engine, including constructing the request, sending it to the appropriate endpoint, and processing the response 
 * to return a structured result that can be easily consumed by the service layer.
 */
public interface ImpactEngineClient {

    Map<String, Object> analyze(ImpactEntityType entityType, String entityId);
}
