package com.miae.analysis;

/**
 * Defines a strategy for analyzing the impact of a specific type of entity (e.g., supplier, component, product).
 * Each implementation of this interface should specify which entity type it supports and provide the logic to analyze
 */
public interface ImpactStrategy {

    /**
     * Returns the entity type that this strategy supports.
     *
     * @return the supported entity type
     */
    ImpactEntityType supports();

    /**
     * Analyzes the impact of the specified entity.
     *
     * @param entityId the ID of the entity to analyze
     * @return the impact analysis results
     */
    Object analyze(String entityId);
}
