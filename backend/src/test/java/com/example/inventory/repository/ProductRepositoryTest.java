package com.example.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.inventory.domain.Product;
import com.example.inventory.support.H2IntegrationTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@H2IntegrationTest
@Transactional
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Long productId;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        productId = productRepository.save(new Product("SKU-2001", "在庫テスト商品", new BigDecimal("1000"), 5)).getId();
    }

    @Test
    void decreaseStockUpdatesOneRowWhenStockIsSufficient() {
        assertThat(productRepository.decreaseStock(productId, 5)).isEqualTo(1);
    }

    @Test
    void decreaseStockUpdatesNoRowWhenStockIsInsufficient() {
        assertThat(productRepository.decreaseStock(productId, 6)).isZero();
    }
}
