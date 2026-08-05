package com.example.inventory.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * アプリ固有設定。application.yml の app.* を束ねる。
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * 低在庫と判定するしきい値(この値未満で低在庫)。
     * 現状はグローバル設定のみ。将来 Product 側に固有しきい値を持たせ、
     * 設定されていればこの値を上書きする拡張が可能。
     */
    private int lowStockThreshold = 10;

    private final Cors cors = new Cors();

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public Cors getCors() {
        return cors;
    }

    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:5173");

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }
}
