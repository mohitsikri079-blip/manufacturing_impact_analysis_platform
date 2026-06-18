package com.miae.copilot.dto;

import com.miae.analysis.ImpactEntityType;

/**
 * Data transfer object for encapsulating the payload required to perform an impact analysis in the Manufacturing Impact Copilot.
 */
public record ImpactAnalysisPayload(
        ImpactEntityType entityType,
        String entityId
) {
}
