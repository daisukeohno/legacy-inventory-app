package com.example.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.inventory.AbstractPostgresTest;
import com.example.inventory.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest extends AbstractPostgresTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void decreaseStockSucceedsWhenStockIsSufficient() {
        Product product = productRepository.findAll().stream()
                .filter(p -> p.getSku().equals("SKU-1001")).findFirst().orElseThrow();

        int updated = productRepository.decreaseStock(product.getId(), 4);

        assertThat(updated).isEqualTo(1);
        assertThat(currentStock(product.getId())).isEqualTo(20);
    }

    @Test
    void decreaseStockFailsWithoutChangingStockWhenInsufficient() {
        Product product = productRepository.findAll().stream()
                .filter(p -> p.getSku().equals("SKU-1003")).findFirst().orElseThrow();

        int updated = productRepository.decreaseStock(product.getId(), 99);

        assertThat(updated).isZero();
        assertThat(currentStock(product.getId())).isEqualTo(3);
    }

    @Test
    void searchFiltersByKeywordAndThreshold() {
        assertThat(productRepository.search("", Integer.MAX_VALUE)).hasSize(6);
        assertThat(productRepository.search("マウス", Integer.MAX_VALUE))
                .extracting(Product::getSku).containsExactly("SKU-1002");
        assertThat(productRepository.search("sku-100", Integer.MAX_VALUE)).hasSize(6);
        assertThat(productRepository.search("", 10))
                .extracting(Product::getSku).containsExactly("SKU-1002", "SKU-1003", "SKU-1005");
    }

    private int currentStock(Long id) {
        return jdbcTemplate.queryForObject("SELECT stock_quantity FROM product WHERE id = ?",
                Integer.class, id);
    }
}
