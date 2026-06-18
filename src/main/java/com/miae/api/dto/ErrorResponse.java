package com.miae.api.dto;

import java.time.Instant;
import java.util.List;

/**
 * Data transfer object for representing error responses from the API. This DTO includes a timestamp, HTTP status code, error message, detailed message, request path, and an optional list of field errors for validation failures.
 * <p>FieldError is a nested record that contains the name of the field that caused
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {
    }
}
