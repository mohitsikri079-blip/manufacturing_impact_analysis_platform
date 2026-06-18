package com.miae.graph.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * Node entity representing a customer in the manufacturing neo4j graph.
 */
@Node("CUSTOMER")
public class CustomerNode {

    @Id
    private String customerId;
    private String customerName;

    public CustomerNode() {
    }

    public CustomerNode(String customerId, String customerName) {
        this.customerId = customerId;
        this.customerName = customerName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }
}
