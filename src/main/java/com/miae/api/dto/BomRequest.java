package com.miae.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * Data transfer object for creating or updating a bill of materials (BOM) for a specific product revision.
 * This DTO contains the revision ID and a list of components that make up the BOM, along with their respective quantities. 
 * <p>Each component is represented by a nested BomComponentRequest record that includes the component ID and quantity required for the revision. 
 * <p>Validation annotations ensure that the necessary fields are provided and that quantities are positive values.
 */
public record BomRequest(
        @NotBlank String revisionId,
        @NotEmpty List<@Valid BomComponentRequest> components
) {
    public record BomComponentRequest(
            @NotBlank String componentId,
            @NotNull @Positive Long quantity
    ) {
    }
}
