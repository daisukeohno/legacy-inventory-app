package com.example.inventory.repository;

import com.example.inventory.domain.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    /**
     * keyword は空文字を「絞り込みなし」として扱う。null を渡すと PostgreSQL 側で
     * パラメータの型が決まらず lower(bytea) エラーになるため、呼び出し元で正規化する。
     */
    @Query("SELECT p FROM Product p "
            + "WHERE (:keyword = '' "
            + "       OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "       OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "  AND (:lowStockOnly = FALSE OR p.stockQuantity < :threshold) "
            + "ORDER BY p.id")
    List<Product> search(@Param("keyword") String keyword,
                         @Param("lowStockOnly") boolean lowStockOnly,
                         @Param("threshold") int threshold);

    /**
     * 在庫のアトミック引き落とし。更新件数0件は在庫不足を意味する。
     */
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :qty "
            + "WHERE p.id = :id AND p.stockQuantity >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") int quantity);
}
