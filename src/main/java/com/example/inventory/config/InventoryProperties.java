package com.example.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 業務パラメータの外部化。低在庫しきい値は旧 Product.isLowStock() のマジックナンバー 10 が既定値。
 */
@ConfigurationProperties(prefix = "inventory")
public record InventoryProperties(Integer lowStockThreshold) {

    public static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    public int lowStockThresholdOrDefault() {
        return lowStockThreshold == null ? DEFAULT_LOW_STOCK_THRESHOLD : lowStockThreshold;
    }
}
