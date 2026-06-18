package com.miae.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.miae.validation.Priority;
import com.miae.validation.WorkOrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

/**
 * Data transfer object for creating or updating a work order. This DTO contains the work order ID, associated revision ID, work order status, remaining quantity to produce, priority, and planned completion date for the work order.
 * <p>Validation annotations ensure that all fields are provided in the request, that the remaining quantity is a non-negative value, and that the planned completion date is in the correct format. 
 * <p>The WorkOrderController uses this DTO to receive work order upsert requests and pass them to the WorkOrderService, which then creates or updates the work order in the database accordingly.
 * If the specified revision does not exist during an upsert operation, a ResourceNotFoundException is thrown.
 */
public record WorkOrderRequest(
        @NotBlank String workOrderId,
        @NotBlank String revisionId,
        @NotNull WorkOrderStatus status,
        @NotNull @PositiveOrZero Long remainingQty,
        @NotNull Priority priority,
        @NotNull @JsonFormat(pattern = "dd/MM/yyyy") LocalDate plannedCompletionDate
) {
}
