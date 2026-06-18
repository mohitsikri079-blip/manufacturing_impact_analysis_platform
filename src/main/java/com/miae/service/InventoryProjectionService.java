package com.miae.service;

import com.miae.api.dto.InventoryRequest;
import com.miae.graph.repository.ManufacturingGraphRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class responsible for handling the business logic related to inventory projection in the manufacturing graph.
 * It interacts with the ManufacturingGraphRepository to upsert inventory data into the graph database.
 * The service ensures that the inventory information is correctly represented in the graph, allowing for accurate impact analysis and relationship management between mfg nodes.
 */
@Service
public class InventoryProjectionService {

    private final ManufacturingGraphRepository repository;

    public InventoryProjectionService(ManufacturingGraphRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String upsert(InventoryRequest request) {
        repository.upsertInventory(request);
        return request.inventoryId();
    }
}
