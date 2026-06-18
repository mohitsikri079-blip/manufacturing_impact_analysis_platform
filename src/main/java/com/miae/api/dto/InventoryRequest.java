package com.miae.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Data transfer object for updating inventory levels of a specific component in a specific warehouse. This DTO contains the component ID, warehouse name, quantity to update (which can be positive or zero), and an inventory ID for tracking purposes.
 * <p>Validation annotations ensure that the component ID, warehouse name, and inventory ID are provided, and that the quantity is a non-negative value. 
 * <p>The InventoryController uses this DTO to receive inventory update requests and pass them to the InventoryService, which then updates the inventory levels in the database accordingly. If the specified component or warehouse does not exist, a ResourceNotFoundException is thrown.
 */
public record InventoryRequest(
        @NotBlank String componentId,
        @NotBlank String warehouse,
        @NotNull @PositiveOrZero Long quantity,
        @NotBlank String inventoryId
) {
}
