package com.miae.api;

import com.miae.api.dto.AckResponse;
import com.miae.api.dto.PurchaseOrderRequest;
import com.miae.service.OrderProjectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing purchase order data.
 * <p>
 * This controller provides an endpoint for ingesting purchase order information, which allows clients to ingest purchase order records in the system.
 * <p>
 * The upsert operation is handled by the OrderProjectionService, which processes the incoming PurchaseOrderRequest and returns an acknowledgment response indicating the result of the operation.
 * 
 * The endpoint is designed to accept valid purchase order requests {@link PurchaseOrderRequest} and responds with an HTTP 202 Accepted status upon successful processing.   
 */
@RestController
@RequestMapping("/api/v1/purchase-orders")
@Tag(name = "Ingestion", description = "Upserts ERP manufacturing records into the MIAE knowledge graph.")
public class PurchaseOrderController {

    private final OrderProjectionService service;

    public PurchaseOrderController(OrderProjectionService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Upsert purchase order", description = "Creates or updates an open purchase order for a component.")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AckResponse upsert(@Valid @RequestBody PurchaseOrderRequest request) {
        return AckResponse.upserted(service.upsertPurchaseOrder(request));
    }
}
