package com.miae.api;

import com.miae.api.dto.AckResponse;
import com.miae.api.dto.SupplierMappingRequest;
import com.miae.service.SupplierProjectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling supplier related API endpoints.
 * <p>
 * This controller provides an endpoint for ingesting supplier mappings. It accepts a {@link SupplierMappingRequest} and returns an {@link AckResponse} indicating the result of the operation.
 * <p>
 * The controller relies on the {@link SupplierProjectionService} to perform the actual upsert operation on the supplier mapping data.
 */
@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    private final SupplierProjectionService service;

    public SupplierController(SupplierProjectionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AckResponse upsert(@Valid @RequestBody SupplierMappingRequest request) {
        return AckResponse.upserted(service.upsert(request));
    }
}
