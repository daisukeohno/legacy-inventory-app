package com.example.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.inventory.domain.Product;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ProductRepositoryTest {

    private static final int THRESHOLD = 10;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void seedDataIsLoadedByFlyway() {
        assertThat(productRepository.count()).isEqualTo(6);
        assertThat(productRepository.findBySku("SKU-1001")).get()
                .satisfies(p -> assertThat(p.getPrice()).isEqualByComparingTo("128000.00"));
    }

    @Test
    void searchWithoutFiltersReturnsAllOrderedById() {
        List<Product> found = productRepository.search(null, false, THRESHOLD);
        assertThat(found).hasSize(6);
        assertThat(found).extracting(Product::getSku).startsWith("SKU-1001", "SKU-1002");
    }

    @Test
    void searchMatchesNameOrSkuCaseInsensitively() {
        assertThat(productRepository.search("sku-100", false, THRESHOLD)).hasSize(6);
        assertThat(productRepository.search("マウス", false, THRESHOLD))
                .extracting(Product::getSku).containsExactly("SKU-1002");
        assertThat(productRepository.search("存在しない", false, THRESHOLD)).isEmpty();
    }

    @Test
    void searchWithLowStockOnlyReturnsProductsBelowThreshold() {
        assertThat(productRepository.search(null, true, THRESHOLD))
                .extracting(Product::getSku)
                .containsExactly("SKU-1002", "SKU-1003", "SKU-1005");
    }

    @Test
    void searchCombinesKeywordAndLowStockFilter() {
        assertThat(productRepository.search("ハブ", true, THRESHOLD))
                .extracting(Product::getSku).containsExactly("SKU-1003");
        assertThat(productRepository.search("ノートPC", true, THRESHOLD)).isEmpty();
    }

    @Test
    void decreaseStockSucceedsWhenStockIsSufficient() {
        Product target = productRepository.findBySku("SKU-1002").orElseThrow();

        assertThat(productRepository.decreaseStock(target.getId(), 6)).isEqualTo(1);
        assertThat(productRepository.findById(target.getId()).orElseThrow().getStockQuantity()).isZero();
    }

    @Test
    void decreaseStockFailsWhenStockIsInsufficient() {
        Product target = productRepository.findBySku("SKU-1003").orElseThrow();

        assertThat(productRepository.decreaseStock(target.getId(), 4)).isZero();
        assertThat(productRepository.findById(target.getId()).orElseThrow().getStockQuantity()).isEqualTo(3);
    }
}
