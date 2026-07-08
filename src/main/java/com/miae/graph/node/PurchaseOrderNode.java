package com.miae.graph.node;

import java.time.LocalDate;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * Node entity representing a purchase order in the manufacturing neo4j graph.
 */
@Node("PURCHASE_ORDER")
public class PurchaseOrderNode {

    @Id
    private String purchaseOrderId;
    private long openQuantity;
    private String supplierId;
    private LocalDate expectedDeliveryDate;

    public PurchaseOrderNode() {
    }

    public PurchaseOrderNode(String purchaseOrderId, long openQuantity, String supplierId, LocalDate expectedDeliveryDate) {
        this.purchaseOrderId = purchaseOrderId;
        this.openQuantity = openQuantity;
        this.supplierId = supplierId;
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public String getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public long getOpenQuantity() {
        return openQuantity;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }
}
