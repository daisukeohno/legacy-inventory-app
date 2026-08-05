package com.example.inventory.web;

import java.util.List;

public record ErrorResponse(String message, List<FieldErrorDetail> fieldErrors) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, List.of());
    }

    public record FieldErrorDetail(String field, String message) {
    }
}
