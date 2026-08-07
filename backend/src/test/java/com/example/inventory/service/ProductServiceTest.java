package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.inventory.domain.Product;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.support.H2IntegrationTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@H2IntegrationTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        productRepository.save(new Product("SKU-1001", "ノートPC 14インチ", new BigDecimal("128000"), 24));
        productRepository.save(new Product("SKU-1003", "USB-Cハブ (7in1)", new BigDecimal("4500"), 3));
    }

    @Test
    void searchWithoutFiltersReturnsAll() {
        assertThat(productService.search(null, false)).hasSize(2);
        assertThat(productService.search("  ", false)).hasSize(2);
    }

    @Test
    void searchMatchesNameOrSkuCaseInsensitively() {
        assertThat(productService.search("sku-1003", false))
                .extracting(Product::getSku).containsExactly("SKU-1003");
        assertThat(productService.search("ノートPC", false))
                .extracting(Product::getSku).containsExactly("SKU-1001");
    }

    @Test
    void lowStockOnlyFiltersByConfiguredThreshold() {
        assertThat(productService.lowStockThreshold()).isEqualTo(10);
        assertThat(productService.search(null, true))
                .extracting(Product::getSku).containsExactly("SKU-1003");
    }

    @Test
    void priceIsStoredWithScaleZero() {
        Product created = productService.create("SKU-9999", "端数商品", new BigDecimal("999.5"), 5);

        assertThat(created.getPrice()).isEqualTo(new BigDecimal("1000"));
        assertThat(productService.isLowStock(created)).isTrue();
    }
}
