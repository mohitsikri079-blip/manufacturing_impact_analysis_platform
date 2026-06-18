package com.miae.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

/**
 * Embedded Configuration class for setting up Neo4j schema constraints.
 * Also refer to resource file `neo4j_constraints.cypher` for the list of constraints to be applied on the Neo4j database.
 * <p>
 * This class implements ApplicationRunner to execute the schema setup logic after the application context is initialized.
 * It defines a list of Cypher statements to create unique constraints on various node labels and properties used in the manufacturing graph.
*/
@Component
public class Neo4jSchemaConfig implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jSchemaConfig.class);

    private final Neo4jClient neo4jClient;

    public Neo4jSchemaConfig(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> constraints = List.of(
                "CREATE CONSTRAINT miae_product_product_id_unique IF NOT EXISTS FOR (n:PRODUCT) REQUIRE n.productId IS UNIQUE",
                "CREATE CONSTRAINT miae_revision_revision_id_unique IF NOT EXISTS FOR (n:REVISION) REQUIRE n.revisionId IS UNIQUE",
                "CREATE CONSTRAINT miae_component_component_id_unique IF NOT EXISTS FOR (n:COMPONENT) REQUIRE n.componentId IS UNIQUE",
                "CREATE CONSTRAINT miae_supplier_supplier_id_unique IF NOT EXISTS FOR (n:SUPPLIER) REQUIRE n.supplierId IS UNIQUE",
                "CREATE CONSTRAINT miae_inventory_inventory_id_unique IF NOT EXISTS FOR (n:INVENTORY) REQUIRE n.inventoryId IS UNIQUE",
                "CREATE CONSTRAINT miae_purchase_order_purchase_order_id_unique IF NOT EXISTS FOR (n:PURCHASE_ORDER) REQUIRE n.purchaseOrderId IS UNIQUE",
                "CREATE CONSTRAINT miae_work_order_work_order_id_unique IF NOT EXISTS FOR (n:WORK_ORDER) REQUIRE n.workOrderId IS UNIQUE",
                "CREATE CONSTRAINT miae_sales_order_sales_order_id_unique IF NOT EXISTS FOR (n:SALES_ORDER) REQUIRE n.salesOrderId IS UNIQUE",
                "CREATE CONSTRAINT miae_customer_customer_id_unique IF NOT EXISTS FOR (n:CUSTOMER) REQUIRE n.customerId IS UNIQUE"
        );
        constraints.forEach(this::createConstraint);
        LOGGER.info("Neo4j manufacturing graph constraints are ready");
    }

    private void createConstraint(String cypher) {
        try {
            neo4jClient.query(cypher).run();
        } catch (DataAccessException ex) {
            LOGGER.warn("Neo4j constraint creation skipped because an existing schema object conflicts. cypher={}, reason={}",
                    cypher,
                    ex.getMostSpecificCause().getMessage());
        }
    }
}
