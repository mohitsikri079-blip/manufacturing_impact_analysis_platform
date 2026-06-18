package com.miae.graph.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * Node entity representing a revision record in the manufacturing neo4j graph.
 */
@Node("REVISION")
public class RevisionNode {

    @Id
    private String revisionId;
    private String code;
    private String status;
    private String productId;

    public RevisionNode() {
    }

    public RevisionNode(String revisionId, String code, String status, String productId) {
        this.revisionId = revisionId;
        this.code = code;
        this.status = status;
        this.productId = productId;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public String getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public String getProductId() {
        return productId;
    }
}
