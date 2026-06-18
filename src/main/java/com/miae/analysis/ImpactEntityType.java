package com.miae.analysis;

/**
 * Enumeration of entity types that can be analyzed for impact. This is used to determine which ImpactStrategy implementation
 */
public enum ImpactEntityType {
    REVISION,
    COMPONENT,
    SUPPLIER
}
