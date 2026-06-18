package com.miae.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data transfer object for creating or updating a product revision. This DTO contains the revision ID, associated product ID, revision code, and revision status.
 * <p>Validation annotations ensure that all fields are provided in the request. 
 * <p>The RevisionController uses this DTO to receive revision upsert requests and pass them to the RevisionService, which then creates or updates the revision in the database accordingly. 
 * If the specified product does not exist during an upsert operation, a ResourceNotFoundException is thrown.   
 */
public record RevisionRequest(
        @NotBlank String revisionId,
        @NotBlank String productId,
        @NotBlank String code,
        @NotBlank String status
) {
}
