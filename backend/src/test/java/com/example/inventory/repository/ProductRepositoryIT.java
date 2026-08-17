package com.example.inventory.repository;

import com.example.inventory.AbstractPostgresIntegrationTest;
import com.example.inventory.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRepositoryIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void searchWithoutFiltersReturnsAllSeededProductsOrderedById() {
        List<Product> result = productRepository.search("", false, 10);

        assertThat(result).extracting(Product::getSku)
                .containsExactly("SKU-1001", "SKU-1002", "SKU-1003", "SKU-1004", "SKU-1005", "SKU-1006");
    }

    @Test
    void searchMatchesNamePartially() {
        assertThat(productRepository.search("マウス", false, 10))
                .extracting(Product::getSku).containsExactly("SKU-1002");
    }

    @Test
    void searchMatchesSkuCaseInsensitively() {
        assertThat(productRepository.search("sku-1003", false, 10))
                .extracting(Product::getName).containsExactly("USB-Cハブ (7in1)");
    }

    @Test
    void searchAppliesLowStockFilter() {
        assertThat(productRepository.search("", true, 10))
                .extracting(Product::getSku).containsExactly("SKU-1002", "SKU-1003", "SKU-1005");
    }

    @Test
    void searchCombinesKeywordAndLowStockFilterWithAnd() {
        assertThat(productRepository.search("SKU-1001", true, 10)).isEmpty();
        assertThat(productRepository.search("SKU-1002", true, 10)).hasSize(1);
    }

    @Test
    void priceIsPersistedAsNumericWithoutFraction() {
        Product product = productRepository.search("SKU-1001", false, 10).get(0);

        assertThat(product.getPrice()).isEqualByComparingTo("128000");
    }

    @Test
    @Transactional
    void decreaseStockUpdatesWhenEnoughStock() {
        Long id = productRepository.search("SKU-1002", false, 10).get(0).getId();

        assertThat(productRepository.decreaseStock(id, 6)).isEqualTo(1);
        assertThat(productRepository.findById(id).orElseThrow().getStockQuantity()).isZero();
    }

    @Test
    @Transactional
    void decreaseStockRejectsWhenStockInsufficient() {
        Long id = productRepository.search("SKU-1003", false, 10).get(0).getId();

        assertThat(productRepository.decreaseStock(id, 4)).isZero();
        assertThat(productRepository.findById(id).orElseThrow().getStockQuantity()).isEqualTo(3);
    }
}
