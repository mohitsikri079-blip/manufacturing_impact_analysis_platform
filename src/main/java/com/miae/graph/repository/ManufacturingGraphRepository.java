package com.miae.graph.repository;

import com.miae.api.dto.BomRequest;
import com.miae.api.dto.InventoryRequest;
import com.miae.api.dto.ProductRequest;
import com.miae.api.dto.PurchaseOrderRequest;
import com.miae.api.dto.RevisionRequest;
import com.miae.api.dto.SalesOrderRequest;
import com.miae.api.dto.SupplierMappingRequest;
import com.miae.api.dto.WorkOrderRequest;
import java.util.Map;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

/**
 * Repository class for managing the manufacturing graph in Neo4j. 
 * <p>
 * It provides methods to upsert nodes and relationships based on the incoming data transfer objects (DTOs) for products, revisions, bills of materials, supplier mappings, inventory, purchase orders, work orders, and sales orders.
 * <p>
 * Each method constructs and executes Cypher queries to ensure that the graph is updated with the latest information while maintaining data integrity and relationships
 */
@Repository
public class ManufacturingGraphRepository {

    private final Neo4jClient neo4jClient;

    public ManufacturingGraphRepository(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public void upsertProduct(ProductRequest request) {
        neo4jClient.query("""
                MERGE (p:PRODUCT {productId: $productId})
                SET p.code = $code, p.name = $name
                """)
                .bindAll(Map.of("productId", request.productId(), "code", request.code(), "name", request.name()))
                .run();
    }

    public void upsertRevision(RevisionRequest request) {
        neo4jClient.query("""
                MERGE (p:PRODUCT {productId: $productId})
                MERGE (r:REVISION {revisionId: $revisionId})
                SET r.code = $code, r.status = $status, r.productId = $productId
                MERGE (p)-[:HAS_REVISION]->(r)
                """)
                .bindAll(Map.of(
                        "productId", request.productId(),
                        "revisionId", request.revisionId(),
                        "code", request.code(),
                        "status", request.status()))
                .run();
    }

    public void upsertBom(BomRequest request) {
        for (BomRequest.BomComponentRequest component : request.components()) {
            neo4jClient.query("""
                    MERGE (r:REVISION {revisionId: $revisionId})
                    MERGE (c:COMPONENT {componentId: $componentId})
                    MERGE (r)-[rel:USES_COMPONENT]->(c)
                    SET rel.quantity = $quantity
                    """)
                    .bindAll(Map.of(
                            "revisionId", request.revisionId(),
                            "componentId", component.componentId(),
                            "quantity", component.quantity()))
                    .run();
        }
    }

    public void upsertSupplierMapping(SupplierMappingRequest request) {
        for (SupplierMappingRequest.SupplierRequest supplier : request.suppliers()) {
            neo4jClient.query("""
                    MERGE (c:COMPONENT {componentId: $componentId})
                    MERGE (s:SUPPLIER {supplierId: $supplierId})
                    SET s.supplierName = CASE WHEN $supplierName = '' THEN s.supplierName ELSE $supplierName END
                    MERGE (c)-[rel:SUPPLIED_BY]->(s)
                    SET rel.leadTimeDays = $leadTimeDays
                    """)
                    .bindAll(Map.of(
                            "componentId", request.componentId(),
                            "supplierId", supplier.supplierId(),
                            "supplierName", supplier.supplierName() == null ? "" : supplier.supplierName(),
                            "leadTimeDays", supplier.leadTimeDays()))
                    .run();
        }
    }

    public void upsertInventory(InventoryRequest request) {
        neo4jClient.query("""
                MERGE (c:COMPONENT {componentId: $componentId})
                MERGE (i:INVENTORY {inventoryId: $inventoryId})
                SET i.warehouse = $warehouse, i.quantity = $quantity
                MERGE (i)-[:STOCKS]->(c)
                """)
                .bindAll(Map.of(
                        "componentId", request.componentId(),
                        "inventoryId", request.inventoryId(),
                        "warehouse", request.warehouse(),
                        "quantity", request.quantity()))
                .run();
    }

    public void upsertPurchaseOrder(PurchaseOrderRequest request) {
        neo4jClient.query("""
                MERGE (c:COMPONENT {componentId: $componentId})
                MERGE (s:SUPPLIER {supplierId: $supplierId})
                MERGE (po:PURCHASE_ORDER {purchaseOrderId: $purchaseOrderId})
                SET po.openQuantity = $openQuantity, po.supplierId = $supplierId
                MERGE (po)-[:PURCHASES]->(c)
                MERGE (c)-[:SUPPLIED_BY]->(s)
                """)
                .bindAll(Map.of(
                        "componentId", request.componentId(),
                        "supplierId", request.supplierId(),
                        "purchaseOrderId", request.purchaseOrderId(),
                        "openQuantity", request.openQuantity()))
                .run();
    }

    public void upsertWorkOrder(WorkOrderRequest request) {
        neo4jClient.query("""
                MERGE (r:REVISION {revisionId: $revisionId})
                MERGE (wo:WORK_ORDER {workOrderId: $workOrderId})
                SET wo.status = $status,
                    wo.remainingQuantity = $remainingQuantity,
                    wo.priority = $priority,
                    wo.plannedCompletionDate = date($plannedCompletionDate)
                MERGE (wo)-[:BUILDS]->(r)
                """)
                .bindAll(Map.of(
                        "revisionId", request.revisionId(),
                        "workOrderId", request.workOrderId(),
                        "status", request.status().name(),
                        "remainingQuantity", request.remainingQty(),
                        "priority", request.priority().name(),
                        "plannedCompletionDate", request.plannedCompletionDate().toString()))
                .run();
    }

    public void upsertSalesOrder(SalesOrderRequest request) {
        neo4jClient.query("""
                MERGE (p:PRODUCT {productId: $productId})
                MERGE (c:CUSTOMER {customerId: $customerId})
                SET c.customerName = $customerName
                MERGE (so:SALES_ORDER {salesOrderId: $salesOrderId})
                SET so.openQuantity = $openQuantity,
                    so.orderValue = $orderValue,
                    so.priority = $priority,
                    so.productId = $productId
                MERGE (so)-[:ORDERS]->(p)
                MERGE (c)-[:PLACED]->(so)
                """)
                .bindAll(Map.of(
                        "productId", request.productId(),
                        "customerId", request.customerId(),
                        "customerName", request.customerName(),
                        "salesOrderId", request.salesOrderId(),
                        "openQuantity", request.openQuantity(),
                        "orderValue", request.orderValue().doubleValue(),
                        "priority", request.priority().name()))
                .run();
    }
}
