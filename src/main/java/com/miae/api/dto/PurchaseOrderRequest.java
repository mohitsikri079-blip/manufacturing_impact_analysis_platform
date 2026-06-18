package com.miae.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Data transfer object for creating or updating a purchase order. This DTO contains the purchase order ID, supplier ID, component ID, and open quantity for the purchase order.
 * <p>Validation annotations ensure that all fields are provided in the request and that the open quantity is a non-negative value. 
 * <p>The PurchaseOrderController uses this DTO to receive purchase order upsert requests and pass them to the PurchaseOrderService, which then creates or updates the purchase order in the database accordingly. 
 * If the specified supplier or component does not exist during an upsert operation, a ResourceNotFoundException is thrown.
 */
public record PurchaseOrderRequest(
        @NotBlank String purchaseOrderId,
        @NotBlank String supplierId,
        @NotBlank String componentId,
        @NotNull @PositiveOrZero Long openQuantity
) {
}
