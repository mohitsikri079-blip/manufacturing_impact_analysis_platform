package com.miae.analysis;

import com.miae.api.dto.impact.ComponentImpactResponse;
import com.miae.api.dto.impact.CustomerImpact;
import com.miae.api.dto.impact.InventoryImpact;
import com.miae.api.dto.impact.ManufacturingImpact;
import com.miae.api.dto.impact.ProcurementImpact;
import com.miae.api.dto.impact.ProductUsageImpact;
import com.miae.api.dto.impact.SupplierImpactItem;
import com.miae.api.dto.impact.SalesOrderImpact;
import com.miae.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

/**
 * Impact strategy implementation for analyzing the impact of a component failure. This class queries the Neo4j database to determine
 * which products and revisions use the component, inventory levels, suppliers, open purchase orders, and active work orders that are affected by the component.
 * <p>
 * The results are aggregated into a ComponentImpactResponse object that can be returned to the caller. If the specified component does not exist, a ResourceNotFoundException is thrown. 
 */
@Component
public class ComponentImpactStrategy extends Neo4jAnalysisSupport implements ImpactStrategy {

    public ComponentImpactStrategy(Neo4jClient neo4jClient) {
        super(neo4jClient);
    }

    @Override
    public ImpactEntityType supports() {
        return ImpactEntityType.COMPONENT;
    }

    @Override
    public ComponentImpactResponse analyze(String componentId) {
        Map<String, Object> params = Map.of("componentId", componentId);
        if (!exists("MATCH (c:COMPONENT {componentId: $componentId}) RETURN count(c) > 0 AS exists", params)) {
            throw new ResourceNotFoundException("Component not found: " + componentId);
        }

        List<ProductUsageImpact> productUsage = query("""
                MATCH (p:PRODUCT)-[:HAS_REVISION]->(r:REVISION)-[:USES_COMPONENT]->(:COMPONENT {componentId: $componentId})
                RETURN DISTINCT p.productId AS productId, r.revisionId AS revisionId
                ORDER BY p.productId, r.revisionId
                """, params)
                .fetchAs(ProductUsageImpact.class)
                .mappedBy((typeSystem, record) -> new ProductUsageImpact(
                        nullableString(record, "productId"),
                        nullableString(record, "revisionId")))
                .all()
                .stream()
                .toList();

        List<InventoryImpact> inventory = query("""
                MATCH (i:INVENTORY)-[:STOCKS]->(c:COMPONENT {componentId: $componentId})
                RETURN c.componentId AS componentId, i.warehouse AS warehouse, i.quantity AS quantity, c.uom AS uom
                ORDER BY i.warehouse
                """, params)
                .fetchAs(InventoryImpact.class)
                .mappedBy((typeSystem, record) -> new InventoryImpact(
                        nullableString(record, "componentId"),
                        nullableString(record, "warehouse"),
                        nullableLong(record, "quantity"),
                        nullableString(record, "uom")))
                .all()
                .stream()
                .toList();

        List<SupplierImpactItem> suppliers = query("""
                MATCH (:COMPONENT {componentId: $componentId})-[:SUPPLIED_BY]->(s:SUPPLIER)
                RETURN DISTINCT s.supplierId AS supplierId, s.supplierName AS supplierName
                ORDER BY s.supplierId
                """, params)
                .fetchAs(SupplierImpactItem.class)
                .mappedBy((typeSystem, record) -> new SupplierImpactItem(
                        nullableString(record, "supplierId"),
                        nullableString(record, "supplierName")))
                .all()
                .stream()
                .toList();

        List<ProcurementImpact> purchaseOrders = query("""
                MATCH (po:PURCHASE_ORDER)-[:PURCHASES]->(c:COMPONENT {componentId: $componentId})
                WHERE coalesce(po.openQuantity, 0) > 0
                RETURN po.purchaseOrderId AS purchaseOrderId,
                       c.componentId AS componentId,
                       po.openQuantity AS openQuantity,
                       c.uom AS uom,
                       po.expectedDeliveryDate AS expectedDeliveryDate
                ORDER BY po.purchaseOrderId
                """, params)
                .fetchAs(ProcurementImpact.class)
                .mappedBy((typeSystem, record) -> new ProcurementImpact(
                        nullableString(record, "purchaseOrderId"),
                        nullableString(record, "componentId"),
                        nullableLong(record, "openQuantity"),
                        nullableString(record, "uom"),
                        nullableLocalDate(record, "expectedDeliveryDate")))
                .all()
                .stream()
                .toList();

        List<ManufacturingImpact> workOrders = query("""
                MATCH (wo:WORK_ORDER)-[:BUILDS]->(:REVISION)-[:USES_COMPONENT]->(:COMPONENT {componentId: $componentId})
                WHERE wo.status IN ['CREATED', 'RELEASED', 'IN_PROGRESS']
                RETURN DISTINCT wo.workOrderId AS workOrderId,
                       wo.status AS status,
                       wo.remainingQuantity AS remainingQty,
                       wo.uom AS uom,
                       wo.materialAvailabilityStatus AS materialAvailabilityStatus
                ORDER BY wo.workOrderId
                """, params)
                .fetchAs(ManufacturingImpact.class)
                .mappedBy((typeSystem, record) -> new ManufacturingImpact(
                        nullableString(record, "workOrderId"),
                        nullableString(record, "status"),
                        nullableLong(record, "remainingQty"),
                        nullableString(record, "uom"),
                        nullableString(record, "materialAvailabilityStatus")))
                .all()
                .stream()
                .toList();

        List<SalesOrderImpact> salesOrders = query("""
                MATCH (:COMPONENT {componentId: $componentId})<-[:USES_COMPONENT]-(:REVISION)<-[:HAS_REVISION]-(p:PRODUCT)<-[:ORDERS]-(so:SALES_ORDER)
                WHERE coalesce(so.openQuantity, 0) > 0
                RETURN DISTINCT so.salesOrderId AS salesOrderId, so.orderValue AS orderValue, so.currency AS currency
                ORDER BY so.salesOrderId
                """, params)
                .fetchAs(SalesOrderImpact.class)
                .mappedBy((typeSystem, record) -> new SalesOrderImpact(
                        nullableString(record, "salesOrderId"),
                        nullableDecimal(record, "orderValue"),
                        nullableString(record, "currency")))
                .all()
                .stream()
                .toList();

        List<CustomerImpact> customers = query("""
                MATCH (:COMPONENT {componentId: $componentId})<-[:USES_COMPONENT]-(:REVISION)<-[:HAS_REVISION]-(p:PRODUCT)<-[:ORDERS]-(so:SALES_ORDER)<-[:PLACED]-(c:CUSTOMER)
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

        ComponentImpactResponse.Summary summary = new ComponentImpactResponse.Summary(
                productUsage.stream().map(ProductUsageImpact::productId).distinct().count(),
                productUsage.stream().map(ProductUsageImpact::revisionId).distinct().count(),
                inventory.size(),
                purchaseOrders.size(),
                workOrders.size(),
                productionQuantityAtRisk(workOrders),
                suppliers.size(),
                salesOrders.size(),
                customers.stream().map(CustomerImpact::customerId).distinct().count(),
                revenueAtRisk,
                singleCurrency(salesOrders));
        return new ComponentImpactResponse(
                ImpactEntityType.COMPONENT,
                componentId,
                summary,
                productUsage,
                inventory,
                suppliers,
                purchaseOrders,
                workOrders,
                salesOrders,
                customers);
    }

    private String singleCurrency(List<SalesOrderImpact> salesOrders) {
        List<String> currencies = salesOrders.stream()
                .map(SalesOrderImpact::currency)
                .filter(currency -> currency != null && !currency.isBlank())
                .distinct()
                .toList();
        return currencies.size() == 1 ? currencies.getFirst() : null;
    }
}
