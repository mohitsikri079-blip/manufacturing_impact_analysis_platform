package com.miae.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data transfer object for creating or updating a product. This DTO contains the product ID, code, and name.
 * <p>Validation annotations ensure that all fields are provided in the request. 
 * <p>The ProductController uses this DTO to receive product upsert requests and pass them to  the ProductService, which then creates or updates the product in the database accordingly. 
 * If the specified product does not exist during an update operation, a ResourceNotFoundException is thrown.
 */
public record ProductRequest(
        @NotBlank String productId,
        @NotBlank String code,
        @NotBlank String name
) {
}
