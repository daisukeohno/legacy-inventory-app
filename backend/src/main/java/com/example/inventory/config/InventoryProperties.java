package com.example.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Global inventory settings.
 *
 * lowStockThreshold is a single global value. A future extension could add a
 * per-product product.low_stock_threshold column and fall back to this value
 * when the column is null.
 */
@ConfigurationProperties(prefix = "inventory")
public class InventoryProperties {

    private int lowStockThreshold = 10;

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }
}
