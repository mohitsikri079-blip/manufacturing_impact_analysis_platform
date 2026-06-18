package com.miae.analysis;

import com.miae.api.dto.impact.ComponentImpactItem;
import com.miae.api.dto.impact.CustomerImpact;
import com.miae.api.dto.impact.ManufacturingImpact;
import com.miae.api.dto.impact.ProductImpactItem;
import com.miae.api.dto.impact.SalesOrderImpact;
import com.miae.api.dto.impact.SupplierImpactResponse;
import com.miae.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

/**
 * Impact strategy implementation for analyzing the impact of a supplier disruption. This class queries the Neo4j database to determine
 * which components are supplied by the supplier, which products use those components, active work orders that are building affected products, open sales orders for affected products, and customers that are impacted by the supplier disruption.
 * <p>
 * The results are aggregated into a SupplierImpactResponse object that can be returned to the caller. If the specified supplier does not exist, a ResourceNotFoundException is thrown.
 */
@Component
public class SupplierImpactStrategy extends Neo4jAnalysisSupport implements ImpactStrategy {

    public SupplierImpactStrategy(Neo4jClient neo4jClient) {
        super(neo4jClient);
    }

    @Override
    public ImpactEntityType supports() {
        return ImpactEntityType.SUPPLIER;
    }

    @Override
    public SupplierImpactResponse analyze(String supplierId) {
        Map<String, Object> params = Map.of("supplierId", supplierId);
        if (!exists("MATCH (s:SUPPLIER {supplierId: $supplierId}) RETURN count(s) > 0 AS exists", params)) {
            throw new ResourceNotFoundException("Supplier not found: " + supplierId);
        }

        List<ComponentImpactItem> components = query("""
                MATCH (c:COMPONENT)-[:SUPPLIED_BY]->(:SUPPLIER {supplierId: $supplierId})
                RETURN DISTINCT c.componentId AS componentId
                ORDER BY c.componentId
                """, params)
                .fetchAs(ComponentImpactItem.class)
                .mappedBy((typeSystem, record) -> new ComponentImpactItem(nullableString(record, "componentId")))
                .all()
                .stream()
                .toList();

        List<ProductImpactItem> products = query("""
                MATCH (p:PRODUCT)-[:HAS_REVISION]->(:REVISION)-[:USES_COMPONENT]->(:COMPONENT)-[:SUPPLIED_BY]->(:SUPPLIER {supplierId: $supplierId})
                RETURN DISTINCT p.productId AS productId, p.name AS productName
                ORDER BY p.productId
                """, params)
                .fetchAs(ProductImpactItem.class)
                .mappedBy((typeSystem, record) -> new ProductImpactItem(
                        nullableString(record, "productId"),
                        nullableString(record, "productName")))
                .all()
                .stream()
                .toList();

        List<ManufacturingImpact> workOrders = query("""
                MATCH (wo:WORK_ORDER)-[:BUILDS]->(:REVISION)-[:USES_COMPONENT]->(:COMPONENT)-[:SUPPLIED_BY]->(:SUPPLIER {supplierId: $supplierId})
                WHERE wo.status IN ['CREATED', 'RELEASED', 'IN_PROGRESS']
                RETURN DISTINCT wo.workOrderId AS workOrderId, wo.status AS status, wo.remainingQuantity AS remainingQty
                ORDER BY wo.workOrderId
                """, params)
                .fetchAs(ManufacturingImpact.class)
                .mappedBy((typeSystem, record) -> new ManufacturingImpact(
                        nullableString(record, "workOrderId"),
                        nullableString(record, "status"),
                        nullableLong(record, "remainingQty")))
                .all()
                .stream()
                .toList();

        List<SalesOrderImpact> salesOrders = query("""
                MATCH (:SUPPLIER {supplierId: $supplierId})<-[:SUPPLIED_BY]-(:COMPONENT)<-[:USES_COMPONENT]-(:REVISION)<-[:HAS_REVISION]-(p:PRODUCT)<-[:ORDERS]-(so:SALES_ORDER)
                WHERE coalesce(so.openQuantity, 0) > 0
                RETURN DISTINCT so.salesOrderId AS salesOrderId, so.orderValue AS orderValue
                ORDER BY so.salesOrderId
                """, params)
                .fetchAs(SalesOrderImpact.class)
                .mappedBy((typeSystem, record) -> new SalesOrderImpact(
                        nullableString(record, "salesOrderId"),
                        nullableDecimal(record, "orderValue")))
                .all()
                .stream()
                .toList();

        List<CustomerImpact> customers = query("""
                MATCH (:SUPPLIER {supplierId: $supplierId})<-[:SUPPLIED_BY]-(:COMPONENT)<-[:USES_COMPONENT]-(:REVISION)<-[:HAS_REVISION]-(p:PRODUCT)<-[:ORDERS]-(so:SALES_ORDER)<-[:PLACED]-(c:CUSTOMER)
                WHERE coalesce(so.openQuantity, 0) > 0
                RETURN DISTINCT c.customerId AS customerId, c.customerName AS customerName, so.salesOrderId AS salesOrderId
                ORDER BY c.customerId, so.salesOrderId
                """, params)
                .fetchAs(CustomerImpact.class)
                .mappedBy((typeSystem, record) -> new CustomerImpact(
                        nullableString(record, "customerId"),
                        nullableString(record, "customerName"),
                        nullableString(record, "salesOrderId")))
                .all()
                .stream()
                .toList();

        BigDecimal revenueAtRisk = salesOrders.stream()
                .map(SalesOrderImpact::orderValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        SupplierImpactResponse.Summary summary = new SupplierImpactResponse.Summary(
                components.size(),
                products.size(),
                workOrders.size(),
                salesOrders.size(),
                customers.stream().map(CustomerImpact::customerId).distinct().count(),
                revenueAtRisk);
        return new SupplierImpactResponse(
                ImpactEntityType.SUPPLIER,
                supplierId,
                summary,
                components,
                products,
                workOrders,
                salesOrders,
                customers);
    }
}
