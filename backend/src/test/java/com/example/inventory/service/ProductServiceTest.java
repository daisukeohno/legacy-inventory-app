package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.inventory.dto.ProductDto;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Test
    void returnsAllProductsWhenNoFilterGiven() {
        assertThat(productService.search(null, false)).hasSize(6);
        assertThat(productService.search("   ", false)).hasSize(6);
    }

    @Test
    void filtersByKeywordAgainstNameAndSkuIgnoringCase() {
        List<ProductDto> bySku = productService.search("sku-1003", false);
        assertThat(bySku).extracting(ProductDto::name).containsExactly("USB-Cハブ (7in1)");

        List<ProductDto> byName = productService.search("モニター", false);
        assertThat(byName).extracting(ProductDto::sku).containsExactly("SKU-1005");
    }

    @Test
    void filtersLowStockOnly() {
        List<ProductDto> lowStock = productService.search(null, true);

        assertThat(lowStock).allMatch(ProductDto::lowStock);
        assertThat(lowStock).extracting(ProductDto::sku)
                .containsExactly("SKU-1002", "SKU-1003", "SKU-1005");
    }

    @Test
    void combinesKeywordAndLowStockFilters() {
        assertThat(productService.search("SKU-100", true)).hasSize(3);
        assertThat(productService.search("ノートPC", true)).isEmpty();
    }
}
