package com.miae.api.dto.impact;

import com.miae.analysis.ImpactEntityType;
import java.util.List;

public record ComponentImpactResponse(
        ImpactEntityType entityType,
        String entityId,
        Summary summary,
        List<ProductUsageImpact> productUsage,
        List<InventoryImpact> inventory,
        List<SupplierImpactItem> suppliers,
        List<ProcurementImpact> purchaseOrders,
        List<ManufacturingImpact> workOrders
) {
    public record Summary(
            long usedByProducts,
            long usedByRevisions,
            long inventoryRecords,
            long affectedPurchaseOrders,
            long affectedWorkOrders,
            long suppliers
    ) {
    }
}
