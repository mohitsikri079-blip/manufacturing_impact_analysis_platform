package com.miae.api;

import com.miae.api.dto.AckResponse;
import com.miae.api.dto.InventoryRequest;
import com.miae.service.InventoryProjectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing inventory data.
 * <p> 
 * This controller provides an endpoint for upserting inventory information, which allows clients to ingest inventory records in the system. 
 * <p>
 * The upsert operation is handled by the InventoryProjectionService, which processes the incoming InventoryRequest and returns an acknowledgment response indicating the result of the operation.
 * <p>
 * The endpoint is designed to accept valid inventory requests {@link InventoryRequest} and responds with an HTTP 202 Accepted status upon successful processing. 
 */
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryProjectionService service;

    public InventoryController(InventoryProjectionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AckResponse upsert(@Valid @RequestBody InventoryRequest request) {
        return AckResponse.upserted(service.upsert(request));
    }
}
