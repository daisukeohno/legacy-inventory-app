package com.example.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.inventory")
public record InventoryProperties(int lowStockThreshold) {
}
