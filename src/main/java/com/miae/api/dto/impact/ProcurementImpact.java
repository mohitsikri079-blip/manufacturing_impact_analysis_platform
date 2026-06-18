package com.miae.api.dto.impact;

public record ProcurementImpact(String purchaseOrderId, String componentId, long openQuantity) {
}
