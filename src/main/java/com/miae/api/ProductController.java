package com.miae.api;

import com.miae.api.dto.AckResponse;
import com.miae.api.dto.ProductRequest;
import com.miae.service.ProductProjectionService;
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
@Tag(name = "Ingestion", description = "Upserts ERP manufacturing records into the MIAE knowledge graph.")
public class ProductController {

    private final ProductProjectionService service;

    public ProductController(ProductProjectionService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Upsert product", description = "Creates or updates a product record. Returns HTTP 202 when accepted.")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AckResponse upsert(@Valid @RequestBody ProductRequest request) {
        return AckResponse.upserted(service.upsert(request));
    }
}
