package com.miae.service;

import com.miae.api.dto.BomRequest;
import com.miae.graph.repository.ManufacturingGraphRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class responsible for handling the business logic related to the Bill of Materials (BOM) projection in the manufacturing graph.
 * It interacts with the ManufacturingGraphRepository to upsert BOM data into the graph database.
 * The service ensures that the BOM information is correctly represented in the graph, allowing for accurate impact analysis and relationship management between mfg nodes.
 */
@Service
public class BomProjectionService {

    private final ManufacturingGraphRepository repository;

    public BomProjectionService(ManufacturingGraphRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String upsert(BomRequest request) {
        repository.upsertBom(request);
        return request.revisionId();
    }
}
