package com.miae.api;

import com.miae.api.dto.AckResponse;
import com.miae.api.dto.ProductRequest;
import com.miae.service.ProductProjectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing product data. 
 * <p>
 * This controller provides an endpoint for upserting product information, which allows clients to ingest product records in the system. 
 * <p>
 * The upsert operation is handled by the ProductProjectionService, which processes the incoming ProductRequest and returns an acknowledgment response indicating the result of the operation. 
 * <p>
 * The endpoint is designed to accept valid product requests {@link ProductRequest} and responds with an HTTP 202 Accepted status upon successful processing.
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductProjectionService service;

    public ProductController(ProductProjectionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AckResponse upsert(@Valid @RequestBody ProductRequest request) {
        return AckResponse.upserted(service.upsert(request));
    }
}
