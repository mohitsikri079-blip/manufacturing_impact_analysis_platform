package com.miae.api.dto.impact;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.miae.analysis.ImpactEntityType;
import java.math.BigDecimal;
import java.util.List;

public record SupplierImpactResponse(
        ImpactEntityType entityType,
        String entityId,
        Summary summary,
        List<ComponentImpactItem> components,
        List<ProductImpactItem> affectedProducts,
        List<ManufacturingImpact> affectedWorkOrders,
        List<SalesOrderImpact> affectedSalesOrders,
        List<CustomerImpact> affectedCustomers
) {
    public record Summary(
            long suppliedComponents,
            long affectedProducts,
            long affectedWorkOrders,
            @JsonInclude(JsonInclude.Include.NON_NULL) Long productionQuantityAtRisk,
            long affectedSalesOrders,
            long affectedCustomers,
            BigDecimal revenueAtRisk,
            String revenueCurrency
    ) {
    }
}
