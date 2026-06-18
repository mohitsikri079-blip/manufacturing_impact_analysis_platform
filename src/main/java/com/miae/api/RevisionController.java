package com.miae.api;

import com.miae.api.dto.AckResponse;
import com.miae.api.dto.RevisionRequest;
import com.miae.service.RevisionProjectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing revision data.
 * <p>
 * This controller provides an endpoint for ingesting revision information, which allows clients to ingest revision records in the system.
 * <p>
 * The upsert operation is handled by the RevisionProjectionService, which processes the incoming RevisionRequest and returns an acknowledgment response indicating the result of the operation.
 * <p>
 * The endpoint is designed to accept valid revision requests {@link RevisionRequest} and responds with an HTTP 202 Accepted status upon successful processing.
 */
@RestController
@RequestMapping("/api/v1/revisions")
public class RevisionController {

    private final RevisionProjectionService service;

    public RevisionController(RevisionProjectionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AckResponse upsert(@Valid @RequestBody RevisionRequest request) {
        return AckResponse.upserted(service.upsert(request));
    }
}
