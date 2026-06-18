package com.miae.analysis;

import com.miae.api.dto.impact.ComponentImpactResponse;
import com.miae.api.dto.impact.InventoryImpact;
import com.miae.api.dto.impact.ManufacturingImpact;
import com.miae.api.dto.impact.ProcurementImpact;
import com.miae.api.dto.impact.ProductUsageImpact;
import com.miae.api.dto.impact.SupplierImpactItem;
import com.miae.exception.ResourceNotFoundException;
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
                RETURN c.componentId AS componentId, i.warehouse AS warehouse, i.quantity AS quantity
                ORDER BY i.warehouse
                """, params)
                .fetchAs(InventoryImpact.class)
                .mappedBy((typeSystem, record) -> new InventoryImpact(
                        nullableString(record, "componentId"),
                        nullableString(record, "warehouse"),
                        nullableLong(record, "quantity")))
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
                RETURN po.purchaseOrderId AS purchaseOrderId, c.componentId AS componentId, po.openQuantity AS openQuantity
                ORDER BY po.purchaseOrderId
                """, params)
                .fetchAs(ProcurementImpact.class)
                .mappedBy((typeSystem, record) -> new ProcurementImpact(
                        nullableString(record, "purchaseOrderId"),
                        nullableString(record, "componentId"),
                        nullableLong(record, "openQuantity")))
                .all()
                .stream()
                .toList();

        List<ManufacturingImpact> workOrders = query("""
                MATCH (wo:WORK_ORDER)-[:BUILDS]->(:REVISION)-[:USES_COMPONENT]->(:COMPONENT {componentId: $componentId})
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

        ComponentImpactResponse.Summary summary = new ComponentImpactResponse.Summary(
                productUsage.stream().map(ProductUsageImpact::productId).distinct().count(),
                productUsage.stream().map(ProductUsageImpact::revisionId).distinct().count(),
                inventory.size(),
                purchaseOrders.size(),
                workOrders.size(),
                suppliers.size());
        return new ComponentImpactResponse(
                ImpactEntityType.COMPONENT,
                componentId,
                summary,
                productUsage,
                inventory,
                suppliers,
                purchaseOrders,
                workOrders);
    }
}
