package com.example.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.inventory.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/** Flywayで投入されたシードデータに対してクエリメソッドを検証する。 */
@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("シードデータは商品6件、金額はNUMERIC(15,2)")
    void seedData() {
        assertThat(productRepository.findAllByOrderByIdAsc()).hasSize(6);
        assertThat(productRepository.findById(1L).orElseThrow().getPrice())
                .isEqualByComparingTo("128000.00");
    }

    @Test
    @DisplayName("在庫しきい値未満の商品を取得する")
    void findLowStock() {
        assertThat(productRepository.findByStockQuantityLessThanOrderByIdAsc(10))
                .extracting(Product::getSku).containsExactly("SKU-1002", "SKU-1003", "SKU-1005");
    }

    @Test
    @DisplayName("商品名またはSKUの部分一致で検索する")
    void findByKeyword() {
        assertThat(productRepository
                .findByNameContainingIgnoreCaseOrSkuContainingIgnoreCaseOrderByIdAsc("キーボード", "キーボード"))
                .extracting(Product::getSku).containsExactly("SKU-1006");
    }
}
