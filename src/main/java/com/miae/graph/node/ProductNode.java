package com.miae.graph.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * Node entity representing a product in the manufacturing neo4j graph.
 */
@Node("PRODUCT")
public class ProductNode {

    @Id
    private String productId;
    private String code;
    private String name;

    public ProductNode() {
    }

    public ProductNode(String productId, String code, String name) {
        this.productId = productId;
        this.code = code;
        this.name = name;
    }

    public String getProductId() {
        return productId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
