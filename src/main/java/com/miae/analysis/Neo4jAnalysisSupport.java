package com.miae.analysis;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.Neo4jClient;

/**
 * Base class for Neo4j analysis strategies that provides common utility methods for executing Cypher queries and mapping results. This class is extended by specific impact strategy implementations such as 
 * ComponentImpactStrategy, RevisionImpactStrategy, and SupplierImpactStrategy to perform analysis based on different entity types. 
 * <p>
 * The utility methods include checking for the existence of nodes, retrieving lists of strings from query results, and safely extracting nullable string, long, and decimal values from query records. 
 * By centralizing these common operations, this class helps reduce code duplication and simplifies the implementation of individual impact strategies.
 */
abstract class Neo4jAnalysisSupport {

    protected final Neo4jClient neo4jClient;

    protected Neo4jAnalysisSupport(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    protected boolean exists(String cypher, Map<String, Object> params) {
        return neo4jClient.query(cypher)
                .bindAll(params)
                .fetchAs(Boolean.class)
                .mappedBy((typeSystem, record) -> record.get("exists").asBoolean())
                .one()
                .orElse(false);
    }

    protected List<String> stringList(String cypher, Map<String, Object> params, String field) {
        return neo4jClient.query(cypher)
                .bindAll(params)
                .fetchAs(String.class)
                .mappedBy((typeSystem, record) -> nullableString(record, field))
                .all()
                .stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();
    }

    protected String nullableString(Record record, String field) {
        Value value = record.get(field);
        return value.isNull() ? null : value.asString();
    }

    protected long nullableLong(Record record, String field) {
        Value value = record.get(field);
        return value.isNull() ? 0L : value.asLong();
    }

    protected BigDecimal nullableDecimal(Record record, String field) {
        Value value = record.get(field);
        if (value.isNull()) {
            return BigDecimal.ZERO;
        }
        Object raw = value.asObject();
        if (raw instanceof BigDecimal decimal) {
            return decimal;
        }
        if (raw instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(raw.toString());
    }
}
