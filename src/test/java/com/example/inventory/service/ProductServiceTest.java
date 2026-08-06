package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.inventory.domain.Product;
import com.example.inventory.dto.ProductRequest;
import com.example.inventory.repository.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void lowStockThresholdDefaultsToTen() {
        assertThat(productService.getLowStockThreshold()).isEqualTo(10);
    }

    @Test
    void searchAppliesKeywordAndLowStockFilter() {
        assertThat(productService.search(null, false)).hasSize(6);
        assertThat(productService.search("  ", false)).hasSize(6);
        assertThat(productService.search("SSD", false)).extracting(Product::getSku).containsExactly("SKU-1004");
        assertThat(productService.search(null, true)).extracting(Product::getSku)
                .containsExactly("SKU-1002", "SKU-1003", "SKU-1005");
    }

    @Test
    void createAndUpdatePersistProduct() {
        Product created = productService.create(
                new ProductRequest(" SKU-2001 ", " 追加商品 ", new BigDecimal("1500.50"), 3));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getSku()).isEqualTo("SKU-2001");
        assertThat(created.getName()).isEqualTo("追加商品");
        assertThat(created.isLowStock()).isTrue();

        productService.update(created.getId(),
                new ProductRequest("SKU-2001", "追加商品(改)", new BigDecimal("1600.00"), 50));
        productRepository.flush();

        Product reloaded = productService.findById(created.getId());
        assertThat(reloaded.getName()).isEqualTo("追加商品(改)");
        assertThat(reloaded.getPrice()).isEqualByComparingTo("1600.00");
        assertThat(reloaded.isLowStock()).isFalse();
    }

    @Test
    void findByIdThrowsForUnknownProduct() {
        assertThatThrownBy(() -> productService.findById(9999))
                .isInstanceOf(ProductNotFoundException.class);
    }
}
