package com.miae.service;

import com.miae.api.dto.RevisionRequest;
import com.miae.graph.repository.ManufacturingGraphRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class responsible for handling the business logic related to revision projection in the manufacturing graph.
 * It interacts with the ManufacturingGraphRepository to upsert revision data into the graph database.
 * The service ensures that the revision information is correctly represented in the graph, allowing for accurate impact analysis and relationship management between mfg nodes.
 */
@Service
public class RevisionProjectionService {

    private final ManufacturingGraphRepository repository;

    public RevisionProjectionService(ManufacturingGraphRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String upsert(RevisionRequest request) {
        repository.upsertRevision(request);
        return request.revisionId();
    }
}
