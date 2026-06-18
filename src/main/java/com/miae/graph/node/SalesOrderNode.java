package com.miae.graph.node;

import java.math.BigDecimal;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * Node entity representing a sales order in the manufacturing neo4j graph.
 */
@Node("SALES_ORDER")
public class SalesOrderNode {

    @Id
    private String salesOrderId;
    private long openQuantity;
    private BigDecimal orderValue;
    private String priority;
    private String productId;

    public SalesOrderNode() {
    }

    public SalesOrderNode(String salesOrderId, long openQuantity, BigDecimal orderValue, String priority, String productId) {
        this.salesOrderId = salesOrderId;
        this.openQuantity = openQuantity;
        this.orderValue = orderValue;
        this.priority = priority;
        this.productId = productId;
    }

    public String getSalesOrderId() {
        return salesOrderId;
    }

    public long getOpenQuantity() {
        return openQuantity;
    }

    public BigDecimal getOrderValue() {
        return orderValue;
    }

    public String getPriority() {
        return priority;
    }

    public String getProductId() {
        return productId;
    }
}
