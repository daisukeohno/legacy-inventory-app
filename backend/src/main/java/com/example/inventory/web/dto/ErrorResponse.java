package com.example.inventory.web.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(int status, String error, String message, List<FieldErrorDto> fieldErrors,
                            OffsetDateTime timestamp) {

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, List.of(), OffsetDateTime.now());
    }

    public static ErrorResponse of(int status, String error, String message, List<FieldErrorDto> fieldErrors) {
        return new ErrorResponse(status, error, message, fieldErrors, OffsetDateTime.now());
    }

    public record FieldErrorDto(String field, String message) {
    }
}
