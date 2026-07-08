package com.miae.graph.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * Node entity representing a component in the manufacturing neo4j graph.
 */
@Node("COMPONENT")
public class ComponentNode {

    @Id
    private String componentId;
    private String uom;

    public ComponentNode() {
    }

    public ComponentNode(String componentId) {
        this.componentId = componentId;
    }

    public ComponentNode(String componentId, String uom) {
        this.componentId = componentId;
        this.uom = uom;
    }

    public String getComponentId() {
        return componentId;
    }

    public String getUom() {
        return uom;
    }
}
