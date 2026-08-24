package com.miae.api.dto.impact;

import com.miae.analysis.ImpactEntityType;
import java.math.BigDecimal;
import java.util.List;

public record ComponentImpactResponse(
        ImpactEntityType entityType,
        String entityId,
        Summary summary,
        List<ProductUsageImpact> productUsage,
        List<InventoryImpact> inventory,
        List<SupplierImpactItem> suppliers,
        List<ProcurementImpact> purchaseOrders,
        List<ManufacturingImpact> workOrders,
        List<SalesOrderImpact> affectedSalesOrders,
        List<CustomerImpact> affectedCustomers
) {
    public record Summary(
            long usedByProducts,
            long usedByRevisions,
            long inventoryRecords,
            long affectedPurchaseOrders,
            long affectedWorkOrders,
            long productionQuantityAtRisk,
            long suppliers,
            long affectedSalesOrders,
            long affectedCustomers,
            BigDecimal revenueAtRisk,
            String revenueCurrency
    ) {
    }
}
