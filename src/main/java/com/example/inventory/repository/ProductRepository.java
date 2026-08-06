package com.example.inventory.repository;

import com.example.inventory.domain.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("""
            SELECT p FROM Product p
            WHERE (:keyword IS NULL
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:lowStockOnly = FALSE OR p.stockQuantity < :threshold)
            ORDER BY p.id
            """)
    List<Product> search(@Param("keyword") String keyword,
                         @Param("lowStockOnly") boolean lowStockOnly,
                         @Param("threshold") int threshold);

    Optional<Product> findBySku(String sku);

    /**
     * 旧 ProductDao.decreaseStock() と同一の「在庫チェック兼引き落とし」SQL。
     * 在庫不足の場合は 0 行更新となり失敗を示す。
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE product SET stock_quantity = stock_quantity - :quantity "
            + "WHERE id = :productId AND stock_quantity >= :quantity", nativeQuery = true)
    int decreaseStock(@Param("productId") int productId, @Param("quantity") int quantity);
}
