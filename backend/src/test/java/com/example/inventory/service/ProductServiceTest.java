package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.inventory.domain.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** 旧 ProductDao.search() / ProductListAction の検索仕様を検証する。 */
@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("キーワード未指定・低在庫フィルタ無しでシードデータ6件を取得する")
    void searchWithoutConditions() {
        List<Product> products = productService.search(null, false);

        assertThat(products).hasSize(6);
        assertThat(products.get(0).getSku()).isEqualTo("SKU-1001");
        assertThat(products.get(0).getPrice()).isEqualByComparingTo("128000.00");
    }

    @Test
    @DisplayName("キーワードは商品名・SKUの部分一致(大文字小文字を区別しない)")
    void searchByKeyword() {
        assertThat(productService.search("マウス", false))
                .extracting(Product::getSku).containsExactly("SKU-1002");
        assertThat(productService.search("sku-100", false)).hasSize(6);
        assertThat(productService.search("usb-c", false))
                .extracting(Product::getSku).containsExactly("SKU-1003");
        assertThat(productService.search("   ", false)).hasSize(6);
        assertThat(productService.search("該当なし", false)).isEmpty();
    }

    @Test
    @DisplayName("低在庫フィルタは在庫10未満の商品のみ返す")
    void searchLowStockOnly() {
        List<Product> products = productService.search(null, true);

        assertThat(products).extracting(Product::getSku)
                .containsExactly("SKU-1002", "SKU-1003", "SKU-1005");
        assertThat(products).allMatch(Product::isLowStock);
    }

    @Test
    @DisplayName("キーワードと低在庫フィルタは併用できる")
    void searchByKeywordAndLowStock() {
        assertThat(productService.search("マウス", true))
                .extracting(Product::getSku).containsExactly("SKU-1002");
        assertThat(productService.search("ノートPC", true)).isEmpty();
    }

    @Test
    @DisplayName("商品の新規登録と更新")
    void createAndUpdate() {
        Product created = productService.create("SKU-9001", "新商品", new BigDecimal("1234.50"), 7);
        assertThat(created.getId()).isNotNull();
        assertThat(created.isLowStock()).isTrue();

        Product updated = productService.update(created.getId(), "SKU-9001", "新商品(改)",
                new BigDecimal("2000.00"), 30);
        assertThat(updated.getName()).isEqualTo("新商品(改)");
        assertThat(updated.getPrice()).isEqualByComparingTo("2000.00");
        assertThat(updated.isLowStock()).isFalse();
        assertThat(productService.findById(created.getId()).getStockQuantity()).isEqualTo(30);
    }

    @Test
    @DisplayName("存在しない商品IDは例外")
    void findByIdNotFound() {
        assertThatThrownBy(() -> productService.findById(999999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
