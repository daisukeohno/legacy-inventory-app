package com.example.inventory.web;

import java.util.List;
import java.util.Map;

public record ApiError(
        int status,
        String message,
        Map<String, String> fieldErrors,
        List<String> details
) {

    public static ApiError of(int status, String message) {
        return new ApiError(status, message, Map.of(), List.of());
    }
}
