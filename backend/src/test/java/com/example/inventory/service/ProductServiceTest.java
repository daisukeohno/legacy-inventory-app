package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.inventory.domain.Product;
import com.example.inventory.repository.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "inventory.low-stock-threshold=7")
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        productRepository.save(new Product("SKU-1001", "ノートPC 14インチ", new BigDecimal("128000"), 24));
        productRepository.save(new Product("SKU-1002", "ワイヤレスマウス", new BigDecimal("2800"), 6));
        productRepository.save(new Product("SKU-1005", "モニター 27インチ 4K", new BigDecimal("46000"), 8));
    }

    @Test
    void lowStockUsesConfiguredThreshold() {
        assertThat(productService.getLowStockThreshold()).isEqualTo(7);
        assertThat(productService.search(null, true)).extracting(Product::getSku).containsExactly("SKU-1002");
    }

    @Test
    void searchMatchesSkuOrName() {
        assertThat(productService.search("  モニター ", false)).extracting(Product::getSku)
                .containsExactly("SKU-1005");
        assertThat(productService.search("   ", false)).hasSize(3);
    }

    @Test
    void createRoundsPriceToScaleZero() {
        Product created = productService.create("SKU-9000", "テスト商品", new BigDecimal("100.5"), 3);
        assertThat(created.getPrice()).isEqualByComparingTo("101");
        assertThat(created.getPrice().scale()).isZero();
        assertThat(productService.isLowStock(created)).isTrue();
    }
}
