package com.example.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.inventory.domain.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product laptop;
    private Product mouse;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        laptop = productRepository.save(new Product("SKU-1001", "ノートPC 14インチ", new BigDecimal("128000"), 24));
        mouse = productRepository.save(new Product("SKU-1002", "ワイヤレスマウス", new BigDecimal("2800"), 6));
    }

    @Test
    void savesAndReadsBack() {
        Product found = productRepository.findBySku("SKU-1001").orElseThrow();
        assertThat(found.getName()).isEqualTo("ノートPC 14インチ");
        assertThat(found.getPrice()).isEqualByComparingTo("128000");
        assertThat(found.getStockQuantity()).isEqualTo(24);
    }

    @Test
    void searchFiltersByKeywordCaseInsensitively() {
        assertThat(productRepository.search("sku-1002", false, 10)).extracting(Product::getId)
                .containsExactly(mouse.getId());
        assertThat(productRepository.search("マウス", false, 10)).extracting(Product::getId)
                .containsExactly(mouse.getId());
        assertThat(productRepository.search("", false, 10)).hasSize(2);
    }

    @Test
    void searchFiltersLowStock() {
        List<Product> lowStock = productRepository.search("", true, 10);
        assertThat(lowStock).extracting(Product::getId).containsExactly(mouse.getId());
    }

    @Test
    void decreaseStockUpdatesRowWhenStockSufficient() {
        int updated = productRepository.decreaseStock(mouse.getId(), 6);
        assertThat(updated).isEqualTo(1);
        assertThat(productRepository.findById(mouse.getId()).orElseThrow().getStockQuantity()).isZero();
    }

    @Test
    void decreaseStockUpdatesNoRowWhenStockInsufficient() {
        int updated = productRepository.decreaseStock(mouse.getId(), 7);
        assertThat(updated).isZero();
        assertThat(productRepository.findById(mouse.getId()).orElseThrow().getStockQuantity()).isEqualTo(6);
        assertThat(laptop.getStockQuantity()).isEqualTo(24);
    }
}
