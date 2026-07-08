package com.miae.api.dto.impact;

import java.time.LocalDate;

public record ProcurementImpact(String purchaseOrderId, String componentId, long openQuantity, String uom, LocalDate expectedDeliveryDate) {
}
