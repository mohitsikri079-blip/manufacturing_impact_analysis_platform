package com.miae.analysis;

import com.miae.api.dto.impact.CustomerImpact;
import com.miae.api.dto.impact.InventoryImpact;
import com.miae.api.dto.impact.ManufacturingImpact;
import com.miae.api.dto.impact.ProcurementImpact;
import com.miae.api.dto.impact.RevisionImpactResponse;
import com.miae.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

/**
 * Impact strategy implementation for analyzing the impact of a revision change. This class queries the Neo4j database to determine
 * which components are added or removed by the revision, inventory levels, suppliers, open purchase orders, active work orders, and customers that are affected by the revision change.
 * <p>
 * The results are aggregated into a RevisionImpactResponse object that can be returned to the caller. If the specified revision does not exist, a ResourceNotFoundException is thrown.
 */
@Component
public class RevisionImpactStrategy extends Neo4jAnalysisSupport implements ImpactStrategy {

    public RevisionImpactStrategy(Neo4jClient neo4jClient) {
        super(neo4jClient);
    }

    @Override
    public ImpactEntityType supports() {
        return ImpactEntityType.REVISION;
    }

    @Override
    public RevisionImpactResponse analyze(String revisionId) {
        Map<String, Object> params = Map.of("revisionId", revisionId);
        if (!exists("MATCH (r:REVISION {revisionId: $revisionId}) RETURN count(r) > 0 AS exists", params)) {
            throw new ResourceNotFoundException("Revision not found: " + revisionId);
        }

        List<String> added = stringList("""
                MATCH (:REVISION {revisionId: $revisionId})-[:USES_COMPONENT]->(c:COMPONENT)
                RETURN c.componentId AS componentId
                """, params, "componentId");
        List<String> removed = stringList("""
                MATCH (p:PRODUCT)-[:HAS_REVISION]->(:REVISION {revisionId: $revisionId})
                MATCH (p)-[:HAS_REVISION]->(other:REVISION)-[:USES_COMPONENT]->(c:COMPONENT)
                WHERE other.revisionId <> $revisionId
                AND NOT (:REVISION {revisionId: $revisionId})-[:USES_COMPONENT]->(c)
                RETURN DISTINCT c.componentId AS componentId
                """, params, "componentId");
        List<String> obsoleteOrCurrentComponents = removed.isEmpty() ? added : removed;

        List<InventoryImpact> inventory = inventoryForComponents(obsoleteOrCurrentComponents);
        List<ProcurementImpact> purchaseOrders = purchaseOrdersForComponents(obsoleteOrCurrentComponents);
        List<ManufacturingImpact> workOrders = query("""
                MATCH (wo:WORK_ORDER)-[:BUILDS]->(:REVISION {revisionId: $revisionId})
                RETURN wo.workOrderId AS workOrderId, wo.status AS status, wo.remainingQuantity AS remainingQty
                ORDER BY wo.priority DESC, wo.workOrderId
                """, params)
                .fetchAs(ManufacturingImpact.class)
                .mappedBy((typeSystem, record) -> new ManufacturingImpact(
                        nullableString(record, "workOrderId"),
                        nullableString(record, "status"),
                        nullableLong(record, "remainingQty")))
                .all()
                .stream()
                .toList();
        List<CustomerImpact> customers = query("""
                MATCH (:REVISION {revisionId: $revisionId})<-[:HAS_REVISION]-(p:PRODUCT)<-[:ORDERS]-(so:SALES_ORDER)<-[:PLACED]-(c:CUSTOMER)
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
        BigDecimal revenueAtRisk = revenueForRevision(params);
        long salesOrderCount = salesOrderCountForRevision(params);
        long blockingWorkOrders = activeWorkOrderCountForRevision(params);
        long blockingSalesOrders = salesOrderCount;

        RevisionImpactResponse.Summary summary = new RevisionImpactResponse.Summary(
                1,
                inventory.size(),
                purchaseOrders.size(),
                workOrders.size(),
                salesOrderCount,
                customers.stream().map(CustomerImpact::customerId).distinct().count(),
                revenueAtRisk);
        return new RevisionImpactResponse(
                ImpactEntityType.REVISION,
                revisionId,
                summary,
                new RevisionImpactResponse.ChangeSummary(added, removed),
                inventory,
                purchaseOrders,
                workOrders,
                customers,
                new RevisionImpactResponse.LifecycleImpact(blockingWorkOrders == 0 && blockingSalesOrders == 0, blockingWorkOrders, blockingSalesOrders));
    }

    private List<InventoryImpact> inventoryForComponents(List<String> componentIds) {
        if (componentIds.isEmpty()) {
            return List.of();
        }
        return query("""
                MATCH (i:INVENTORY)-[:STOCKS]->(c:COMPONENT)
                WHERE c.componentId IN $componentIds
                RETURN c.componentId AS componentId, i.warehouse AS warehouse, i.quantity AS quantity
                ORDER BY c.componentId, i.warehouse
                """, Map.of("componentIds", componentIds))
                .fetchAs(InventoryImpact.class)
                .mappedBy((typeSystem, record) -> new InventoryImpact(
                        nullableString(record, "componentId"),
                        nullableString(record, "warehouse"),
                        nullableLong(record, "quantity")))
                .all()
                .stream()
                .toList();
    }

    private List<ProcurementImpact> purchaseOrdersForComponents(List<String> componentIds) {
        if (componentIds.isEmpty()) {
            return List.of();
        }
        return query("""
                MATCH (po:PURCHASE_ORDER)-[:PURCHASES]->(c:COMPONENT)
                WHERE c.componentId IN $componentIds AND coalesce(po.openQuantity, 0) > 0
                RETURN po.purchaseOrderId AS purchaseOrderId, c.componentId AS componentId, po.openQuantity AS openQuantity
                ORDER BY po.purchaseOrderId
                """, Map.of("componentIds", componentIds))
                .fetchAs(ProcurementImpact.class)
                .mappedBy((typeSystem, record) -> new ProcurementImpact(
                        nullableString(record, "purchaseOrderId"),
                        nullableString(record, "componentId"),
                        nullableLong(record, "openQuantity")))
                .all()
                .stream()
                .toList();
    }

    private BigDecimal revenueForRevision(Map<String, Object> params) {
        return query("""
                MATCH (:REVISION {revisionId: $revisionId})<-[:HAS_REVISION]-(:PRODUCT)<-[:ORDERS]-(so:SALES_ORDER)
                WHERE coalesce(so.openQuantity, 0) > 0
                RETURN coalesce(sum(so.orderValue), 0) AS revenue
                """, params)
                .fetchAs(BigDecimal.class)
                .mappedBy((typeSystem, record) -> nullableDecimal(record, "revenue"))
                .one()
                .orElse(BigDecimal.ZERO);
    }

    private long salesOrderCountForRevision(Map<String, Object> params) {
        return query("""
                MATCH (:REVISION {revisionId: $revisionId})<-[:HAS_REVISION]-(:PRODUCT)<-[:ORDERS]-(so:SALES_ORDER)
                WHERE coalesce(so.openQuantity, 0) > 0
                RETURN count(DISTINCT so) AS count
                """, params)
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> nullableLong(record, "count"))
                .one()
                .orElse(0L);
    }

    private long activeWorkOrderCountForRevision(Map<String, Object> params) {
        return query("""
                MATCH (wo:WORK_ORDER)-[:BUILDS]->(:REVISION {revisionId: $revisionId})
                WHERE wo.status IN ['CREATED', 'RELEASED', 'IN_PROGRESS'] AND coalesce(wo.remainingQuantity, 0) > 0
                RETURN count(DISTINCT wo) AS count
                """, params)
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> nullableLong(record, "count"))
                .one()
                .orElse(0L);
    }
}
