package com.miae.api;

import com.miae.api.dto.AckResponse;
import com.miae.api.dto.SalesOrderRequest;
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
 * Controller for handling sales order related API endpoints.
 * <p>
 * This controller provides an endpoint for ingesting sales orders. It accepts a {@link SalesOrderRequest} and returns an {@link AckResponse} indicating the result of the operation.
 * <p>
 * The controller relies on the {@link OrderProjectionService} to perform the actual upsert operation on the sales order data.   
 */
@RestController
@RequestMapping("/api/v1/sales-orders")
@Tag(name = "Ingestion", description = "Upserts ERP manufacturing records into the MIAE knowledge graph.")
public class SalesOrderController {

    private final OrderProjectionService service;

    public SalesOrderController(OrderProjectionService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Upsert sales order", description = "Creates or updates a customer sales order.")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AckResponse upsert(@Valid @RequestBody SalesOrderRequest request) {
        return AckResponse.upserted(service.upsertSalesOrder(request));
    }
}
