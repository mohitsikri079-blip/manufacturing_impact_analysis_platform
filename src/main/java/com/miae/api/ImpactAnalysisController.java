package com.miae.api;

import com.miae.api.dto.ImpactAnalysisRequest;
import com.miae.service.ImpactAnalysisService;
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
public class ImpactAnalysisController {

    private final ImpactAnalysisService service;

    public ImpactAnalysisController(ImpactAnalysisService service) {
        this.service = service;
    }

    @PostMapping
    public Object analyze(@Valid @RequestBody ImpactAnalysisRequest request) {
        return service.analyze(request);
    }
}
