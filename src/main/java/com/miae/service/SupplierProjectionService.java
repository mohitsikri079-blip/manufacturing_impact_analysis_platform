package com.miae.service;

import com.miae.api.dto.SupplierMappingRequest;
import com.miae.graph.repository.ManufacturingGraphRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class responsible for handling the business logic related to supplier mapping projection in the manufacturing graph.
 * It interacts with the ManufacturingGraphRepository to upsert supplier mapping data into the graph database.
 * The service ensures that the supplier mapping information is correctly represented in the graph, allowing for accurate impact analysis and relationship management between mfg nodes.
 */
@Service
public class SupplierProjectionService {

    private final ManufacturingGraphRepository repository;

    public SupplierProjectionService(ManufacturingGraphRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String upsert(SupplierMappingRequest request) {
        repository.upsertSupplierMapping(request);
        return request.componentId();
    }
}
