package com.miae.graph.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * Node entity representing a supplier in the manufacturing neo4j graph.
 */
@Node("SUPPLIER")
public class SupplierNode {

    @Id
    private String supplierId;
    private String supplierName;

    public SupplierNode() {
    }

    public SupplierNode(String supplierId, String supplierName) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }
}
