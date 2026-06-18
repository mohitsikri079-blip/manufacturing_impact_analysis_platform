package com.miae.service;

import com.miae.api.dto.PurchaseOrderRequest;
import com.miae.api.dto.SalesOrderRequest;
import com.miae.api.dto.WorkOrderRequest;
import com.miae.graph.repository.ManufacturingGraphRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class responsible for handling the business logic related to order projections (purchase orders, work orders, and sales orders) in the manufacturing graph.
 * It interacts with the ManufacturingGraphRepository to upsert order data into the graph database.
 * The service ensures that the order information is correctly represented in the graph, allowing for accurate impact analysis and relationship management between mfg nodes.
 */
@Service
public class OrderProjectionService {

    private final ManufacturingGraphRepository repository;

    public OrderProjectionService(ManufacturingGraphRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String upsertPurchaseOrder(PurchaseOrderRequest request) {
        repository.upsertPurchaseOrder(request);
        return request.purchaseOrderId();
    }

    @Transactional
    public String upsertWorkOrder(WorkOrderRequest request) {
        repository.upsertWorkOrder(request);
        return request.workOrderId();
    }

    @Transactional
    public String upsertSalesOrder(SalesOrderRequest request) {
        repository.upsertSalesOrder(request);
        return request.salesOrderId();
    }
}
