package com.example.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.inventory.entity.Product;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    private Integer productId;

    @BeforeEach
    void setUp() {
        Product product = productRepository.save(
                new Product("SKU-9001", "テスト商品", new BigDecimal("1000"), 5));
        productId = product.getId();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void decreasesStockWhenEnoughOnHand() {
        int updated = productRepository.decreaseStock(productId, 5);

        entityManager.clear();
        assertThat(updated).isEqualTo(1);
        assertThat(productRepository.findById(productId).orElseThrow().getStockQuantity()).isZero();
    }

    @Test
    void doesNotUpdateWhenStockIsInsufficient() {
        int updated = productRepository.decreaseStock(productId, 6);

        entityManager.clear();
        assertThat(updated).isZero();
        assertThat(productRepository.findById(productId).orElseThrow().getStockQuantity()).isEqualTo(5);
    }
}
