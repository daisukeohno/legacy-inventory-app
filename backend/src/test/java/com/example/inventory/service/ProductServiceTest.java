package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.inventory.AbstractPostgresTest;
import com.example.inventory.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductServiceTest extends AbstractPostgresTest {

    @Autowired
    ProductService productService;

    @Test
    void lowStockFilterReturnsOnlyBelowThreshold() {
        assertThat(productService.search(null, true))
                .extracting(Product::getSku)
                .containsExactly("SKU-1002", "SKU-1003", "SKU-1005");
    }

    @Test
    void keywordSearchMatchesNameOrSkuCaseInsensitively() {
        assertThat(productService.search("ssd", false))
                .extracting(Product::getSku).containsExactly("SKU-1004");
        assertThat(productService.search("SKU-1006", false))
                .extracting(Product::getName).containsExactly("メカニカルキーボード");
        assertThat(productService.search("  ", false)).hasSize(6);
    }

    @Test
    void isLowStockUsesConfiguredThreshold() {
        Product low = productService.search("SKU-1003", false).get(0);
        Product high = productService.search("SKU-1001", false).get(0);

        assertThat(productService.isLowStock(low)).isTrue();
        assertThat(productService.isLowStock(high)).isFalse();
        assertThat(productService.lowStockThreshold()).isEqualTo(10);
    }
}
