package com.miae.graph.node;

import static org.assertj.core.api.Assertions.assertThat;

import com.miae.MiaeApplication;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = MiaeApplication.class)
@ActiveProfiles("sft")
@TestPropertySource(properties = {
        "miae.sample-data.enabled=false",
        "spring.neo4j.uri=bolt://localhost:7687",
        "spring.neo4j.authentication.username=neo4j",
        "spring.neo4j.authentication.password=password"
})
class GraphNodeMappingSFT {

    private static final String ID_PREFIX = "NODE-MAPPING-SFT-";

    private final Neo4jClient neo4jClient;

    private final Neo4jOperations neo4jOperations;

    @Autowired
    GraphNodeMappingSFT(Neo4jClient neo4jClient, Neo4jOperations neo4jOperations) {
        this.neo4jClient = neo4jClient;
        this.neo4jOperations = neo4jOperations;
    }

    @BeforeEach
    void setUp() {
        deleteFixtureNodes();
        createFixtureNodes();
    }

    @AfterEach
    void tearDown() {
        deleteFixtureNodes();
    }

    @Test
    void nodeClassesCanBeDeserialized() {
        ProductNode product = findById(ID_PREFIX + "PRODUCT", ProductNode.class);
        RevisionNode revision = findById(ID_PREFIX + "REVISION", RevisionNode.class);
        ComponentNode component = findById(ID_PREFIX + "COMPONENT", ComponentNode.class);
        SupplierNode supplier = findById(ID_PREFIX + "SUPPLIER", SupplierNode.class);
        InventoryNode inventory = findById(ID_PREFIX + "INVENTORY", InventoryNode.class);
        PurchaseOrderNode purchaseOrder = findById(ID_PREFIX + "PO", PurchaseOrderNode.class);
        WorkOrderNode workOrder = findById(ID_PREFIX + "WO", WorkOrderNode.class);
        SalesOrderNode salesOrder = findById(ID_PREFIX + "SO", SalesOrderNode.class);
        CustomerNode customer = findById(ID_PREFIX + "CUSTOMER", CustomerNode.class);

        assertThat(product.getCode()).isEqualTo("SENSOR-MAP");
        assertThat(revision.getProductId()).isEqualTo(product.getProductId());
        assertThat(component.getUom()).isEqualTo("Pcs");
        assertThat(supplier.getSupplierName()).isEqualTo("Mapping Supplier");
        assertThat(inventory.getQuantity()).isEqualTo(125);
        assertThat(purchaseOrder.getExpectedDeliveryDate()).isEqualTo(LocalDate.of(2026, 2, 10));
        assertThat(workOrder.getRemainingQuantity()).isEqualTo(50);
        assertThat(workOrder.getPriority()).isEqualTo("HIGH");
        assertThat(salesOrder.getOrderValue()).isEqualTo(50000.0);
        assertThat(customer.getCustomerName()).isEqualTo("Mapping Customer");
    }

    private <T> T findById(String id, Class<T> nodeType) {
        return neo4jOperations.findById(id, nodeType).orElseThrow();
    }

    @SuppressWarnings("null")
    private void createFixtureNodes() {
        neo4jClient.query("""
                MERGE (p:PRODUCT {productId: $productId})
                SET p.code = 'SENSOR-MAP',
                    p.name = 'Mapping Sensor'
                MERGE (r:REVISION {revisionId: $revisionId})
                SET r.code = 'A',
                    r.status = 'APPROVED',
                    r.productId = $productId
                MERGE (c:COMPONENT {componentId: $componentId})
                SET c.uom = 'Pcs'
                MERGE (s:SUPPLIER {supplierId: $supplierId})
                SET s.supplierName = 'Mapping Supplier'
                MERGE (i:INVENTORY {inventoryId: $inventoryId})
                SET i.warehouse = 'WH-MAP',
                    i.quantity = 125
                MERGE (po:PURCHASE_ORDER {purchaseOrderId: $purchaseOrderId})
                SET po.openQuantity = 1000,
                    po.supplierId = $supplierId,
                    po.expectedDeliveryDate = date('2026-02-10')
                MERGE (wo:WORK_ORDER {workOrderId: $workOrderId})
                SET wo.status = 'RELEASED',
                    wo.remainingQuantity = 50,
                    wo.plannedCompletionDate = date('2026-02-20'),
                    wo.priority = 'HIGH',
                    wo.uom = 'Box',
                    wo.materialAvailabilityStatus = 'READY'
                MERGE (so:SALES_ORDER {salesOrderId: $salesOrderId})
                SET so.openQuantity = 25,
                    so.orderValue = 50000.0,
                    so.priority = 'CRITICAL',
                    so.productId = $productId,
                    so.currency = 'USD'
                MERGE (cust:CUSTOMER {customerId: $customerId})
                SET cust.customerName = 'Mapping Customer'
                """)
                .bindAll(Map.of(
                        "productId", ID_PREFIX + "PRODUCT",
                        "revisionId", ID_PREFIX + "REVISION",
                        "componentId", ID_PREFIX + "COMPONENT",
                        "supplierId", ID_PREFIX + "SUPPLIER",
                        "inventoryId", ID_PREFIX + "INVENTORY",
                        "purchaseOrderId", ID_PREFIX + "PO",
                        "workOrderId", ID_PREFIX + "WO",
                        "salesOrderId", ID_PREFIX + "SO",
                        "customerId", ID_PREFIX + "CUSTOMER"))
                .run();
    }

    private void deleteFixtureNodes() {
        neo4jClient.query("""
                MATCH (n)
                WHERE n.productId STARTS WITH $idPrefix
                   OR n.revisionId STARTS WITH $idPrefix
                   OR n.componentId STARTS WITH $idPrefix
                   OR n.supplierId STARTS WITH $idPrefix
                   OR n.inventoryId STARTS WITH $idPrefix
                   OR n.purchaseOrderId STARTS WITH $idPrefix
                   OR n.workOrderId STARTS WITH $idPrefix
                   OR n.salesOrderId STARTS WITH $idPrefix
                   OR n.customerId STARTS WITH $idPrefix
                DETACH DELETE n
                """)
                .bindAll(Map.of("idPrefix", ID_PREFIX))
                .run();
    }
}
