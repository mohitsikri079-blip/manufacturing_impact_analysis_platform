package com.miae.api;

import com.miae.api.dto.AckResponse;
import com.miae.api.dto.BomRequest;
import com.miae.service.BomProjectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing Bill of Materials (BOM) data. 
 * <p>
 * This controller provides an endpoint for upserting BOM information, which allows clients to ingest BOM records in the system. 
 * <p>
 * The upsert operation is handled by the BomProjectionService, which processes the incoming BomRequest and returns an acknowledgment response indicating the result of the operation. 
 * <p>
 * The endpoint is designed to accept valid BOM requests {@link BomRequest} and responds with an HTTP 202 Accepted status upon successful processing.
 */
@RestController
@RequestMapping("/api/v1/boms")
public class BomController {

    private final BomProjectionService service;

    public BomController(BomProjectionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AckResponse upsert(@Valid @RequestBody BomRequest request) {
        return AckResponse.upserted(service.upsert(request));
    }
}
