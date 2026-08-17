package com.example.inventory.service;

import com.example.inventory.config.InventoryProperties;
import com.example.inventory.domain.Product;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.web.dto.ProductDto;
import com.example.inventory.web.dto.ProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, new InventoryProperties());
    }

    @Test
    void searchNormalizesBlankKeywordToNull() {
        when(productRepository.search(eq(""), eq(false), eq(10))).thenReturn(List.of());

        productService.search("   ", false);

        verify(productRepository).search("", false, 10);
    }

    @Test
    void searchTrimsKeywordAndPassesLowStockFlag() {
        when(productRepository.search(eq("mouse"), eq(true), eq(10))).thenReturn(List.of());

        productService.search(" mouse ", true);

        verify(productRepository).search("mouse", true, 10);
    }

    @Test
    void lowStockIsTrueBelowThresholdOnly() {
        Product below = new Product("SKU-1", "低在庫", new BigDecimal("100"), 9);
        Product atThreshold = new Product("SKU-2", "境界", new BigDecimal("100"), 10);
        when(productRepository.search(eq(""), eq(false), eq(10))).thenReturn(List.of(below, atThreshold));

        List<ProductDto> result = productService.search(null, false);

        assertThat(result).extracting(ProductDto::lowStock).containsExactly(true, false);
    }

    @Test
    void lowStockThresholdIsConfigurable() {
        InventoryProperties properties = new InventoryProperties();
        properties.setLowStockThreshold(5);
        ProductService service = new ProductService(productRepository, properties);
        Product product = new Product("SKU-1", "在庫9", new BigDecimal("100"), 9);
        when(productRepository.search(eq(""), eq(false), eq(5))).thenReturn(List.of(product));

        assertThat(service.search(null, false).get(0).lowStock()).isFalse();
    }

    @Test
    void createTrimsSkuAndName() {
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        productService.create(new ProductRequest("  SKU-9  ", "  新商品  ", new BigDecimal("1200"), 3));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getSku()).isEqualTo("SKU-9");
        assertThat(captor.getValue().getName()).isEqualTo("新商品");
    }

    @Test
    void updateAppliesAllFields() {
        Product existing = new Product("SKU-1", "旧名称", new BigDecimal("100"), 1);
        when(productRepository.findById(7L)).thenReturn(Optional.of(existing));

        ProductDto updated = productService.update(7L,
                new ProductRequest("SKU-2", "新名称", new BigDecimal("2500"), 42));

        assertThat(updated.sku()).isEqualTo("SKU-2");
        assertThat(updated.name()).isEqualTo("新名称");
        assertThat(updated.price()).isEqualByComparingTo("2500");
        assertThat(updated.stockQuantity()).isEqualTo(42);
        assertThat(updated.lowStock()).isFalse();
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }
}
