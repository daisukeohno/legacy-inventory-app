package com.example.inventory.repository;

import com.example.inventory.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 旧 ProductDao.search と同一条件: キーワードが name または sku に部分一致（大文字小文字無視）
     * AND 低在庫フィルタ。キーワードは空文字を渡すと全件が対象になる。
     */
    @Query("""
            SELECT p FROM Product p
            WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:lowStockOnly = FALSE OR p.stockQuantity < :threshold)
            ORDER BY p.id
            """)
    List<Product> search(@Param("keyword") String keyword,
                         @Param("lowStockOnly") boolean lowStockOnly,
                         @Param("threshold") int threshold);

    /**
     * 旧 ProductDao.decreaseStock と同一の条件付きUPDATE。
     * 在庫が足りない場合は 0 件更新となる。
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity
            WHERE p.id = :productId AND p.stockQuantity >= :quantity
            """)
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}
