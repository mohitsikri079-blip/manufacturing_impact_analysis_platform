package com.miae.api;

import com.miae.api.dto.ImpactAnalysisRequest;
import com.miae.service.ImpactAnalysisService;
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
 * REST controller for handling impact analysis requests.
 */
@RestController
@RequestMapping("/api/v1/impact-analysis")
@Tag(name = "Impact analysis", description = "Calculates deterministic downstream manufacturing impact for revisions, components, and suppliers.")
public class ImpactAnalysisController {

    private final ImpactAnalysisService service;

    public ImpactAnalysisController(ImpactAnalysisService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Analyse entity impact", description = "Returns the impact graph and summary for a REVISION, COMPONENT, or SUPPLIER identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Impact analysis completed."),
            @ApiResponse(responseCode = "400", description = "The request is invalid."),
            @ApiResponse(responseCode = "401", description = "Authentication is missing or invalid."),
            @ApiResponse(responseCode = "404", description = "The requested entity does not exist.")
    })
    public Object analyze(@Valid @RequestBody ImpactAnalysisRequest request) {
        return service.analyze(request);
    }
}
