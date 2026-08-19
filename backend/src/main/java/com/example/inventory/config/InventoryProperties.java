package com.example.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 業務パラメータの外部化。旧実装では低在庫しきい値が Product にマジックナンバーで
 * 直書きされていた（stockQuantity &lt; 10）。
 */
@ConfigurationProperties(prefix = "inventory")
public class InventoryProperties {

    /** 低在庫と判定する在庫数のしきい値（この値未満なら低在庫）。 */
    private int lowStockThreshold = 10;

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }
}
