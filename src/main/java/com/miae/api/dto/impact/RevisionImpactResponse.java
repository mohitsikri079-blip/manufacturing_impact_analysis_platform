package com.miae.api.dto.impact;

import com.miae.analysis.ImpactEntityType;
import java.math.BigDecimal;
import java.util.List;

public record RevisionImpactResponse(
        ImpactEntityType entityType,
        String entityId,
        Summary summary,
        ChangeSummary changeSummary,
        List<InventoryImpact> inventoryImpact,
        List<ProcurementImpact> procurementImpact,
        List<ManufacturingImpact> manufacturingImpact,
        List<CustomerImpact> customerImpact,
        LifecycleImpact lifecycleImpact
) {
    public record Summary(
            long affectedProducts,
            long affectedInventoryRecords,
            long affectedPurchaseOrders,
            long affectedWorkOrders,
            long affectedSalesOrders,
            long affectedCustomers,
            BigDecimal revenueAtRisk
    ) {
    }

    public record ChangeSummary(List<String> addedComponents, List<String> removedComponents) {
    }

    public record LifecycleImpact(boolean canRetirePreviousRevision, long blockingWorkOrders, long blockingSalesOrders) {
    }
}
