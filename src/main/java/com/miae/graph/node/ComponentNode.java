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

    public ComponentNode() {
    }

    public ComponentNode(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentId() {
        return componentId;
    }
}
