package com.miae.api.dto;

import com.miae.analysis.ImpactEntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data transfer object for requesting an impact analysis based on a specific entity type and ID. This DTO contains the type of entity to analyze (e.g., COMPONENT, REVISION, SUPPLIER) and the unique identifier of that entity.
 * <p>Validation annotations ensure that both the entity type and ID are provided in the request. 
 * <p>The ImpactAnalysisController uses this DTO to receive requests and pass them to the ImpactAnalysisService, which then delegates to the appropriate ImpactStrategy implementation based on the entity type specified in the request.
 */
public record ImpactAnalysisRequest(
        @NotNull ImpactEntityType entityType,
        @NotBlank String entityId
) {
}
