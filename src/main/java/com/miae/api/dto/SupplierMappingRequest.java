package com.miae.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * Data transfer object for creating or updating supplier mappings for a specific component. This DTO contains the component ID and a list of suppliers that can provide that component, along with their respective lead times.
 * <p>Each supplier is represented by a nested SupplierRequest record that includes the supplier ID and lead time in days. The supplier name is optional and can be included for informational purposes but is not required for the mapping to be valid.
 * <p>Validation annotations ensure that the component ID and supplier list are provided, and that each supplier has a valid ID and non-negative lead time. 
 * <p>The SupplierMappingController uses this DTO to receive supplier mapping upsert requests and pass them to the SupplierMappingService, which then creates or updates the supplier mappings in the database accordingly. If the specified component does not exist during an upsert operation, a ResourceNotFoundException is thrown.
 */
public record SupplierMappingRequest(
        @NotBlank String componentId,
        @NotEmpty List<@Valid SupplierRequest> suppliers
) {
    public record SupplierRequest(
            @NotBlank String supplierId,
            String supplierName,
            @NotNull @PositiveOrZero Integer leadTimeDays
    ) {
    }
}
