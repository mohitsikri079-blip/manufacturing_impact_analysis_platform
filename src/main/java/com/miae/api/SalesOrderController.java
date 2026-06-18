package com.miae.api;

import com.miae.api.dto.AckResponse;
import com.miae.api.dto.SalesOrderRequest;
import com.miae.service.OrderProjectionService;
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
public class SalesOrderController {

    private final OrderProjectionService service;

    public SalesOrderController(OrderProjectionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AckResponse upsert(@Valid @RequestBody SalesOrderRequest request) {
        return AckResponse.upserted(service.upsertSalesOrder(request));
    }
}
