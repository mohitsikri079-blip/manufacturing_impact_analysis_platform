package com.miae.api.dto;

import com.miae.validation.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * Data transfer object for creating or updating a sales order. This DTO contains the sales order ID, customer ID, customer name, product ID, open quantity, order value, and priority for the sales order.
 * <p>Validation annotations ensure that all fields are provided in the request and that the open quantity and order value are non-negative values. 
 * <p>The SalesOrderController uses this DTO to receive sales order upsert requests and pass them to the SalesOrderService, which then creates or updates the sales order in the database accordingly.
 * If the specified customer or product does not exist during an upsert operation, a ResourceNotFoundException is thrown.
 */
public record SalesOrderRequest(
        @NotBlank String salesOrderId,
        @NotBlank String customerId,
        @NotBlank String customerName,
        @NotBlank String productId,
        @NotNull @PositiveOrZero Long openQuantity,
        @NotNull @PositiveOrZero BigDecimal orderValue,
        @NotNull Priority priority
) {
}
