package com.miae.graph.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * Node entity representing an inventory record in the manufacturing neo4j graph.
 */
@Node("INVENTORY")
public class InventoryNode {

    @Id
    private String inventoryId;
    private String warehouse;
    private long quantity;

    public InventoryNode() {
    }

    public InventoryNode(String inventoryId, String warehouse, long quantity) {
        this.inventoryId = inventoryId;
        this.warehouse = warehouse;
        this.quantity = quantity;
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public long getQuantity() {
        return quantity;
    }
}
