package com.miae.api.dto.impact;

public record ManufacturingImpact(String workOrderId, String status, long remainingQty) {
}
