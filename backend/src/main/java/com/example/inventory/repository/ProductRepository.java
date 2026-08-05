package com.example.inventory.repository;

import com.example.inventory.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("""
            SELECT p FROM Product p
            WHERE (:keyword IS NULL
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\'
                   OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\')
              AND (:lowStockOnly = FALSE OR p.stockQuantity < :threshold)
            ORDER BY p.id
            """)
    List<Product> search(@Param("keyword") String keyword,
                         @Param("lowStockOnly") boolean lowStockOnly,
                         @Param("threshold") int threshold);

    /**
     * 在庫が足りている場合のみ引き落とす。更新件数0は在庫不足を意味する。
     */
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :qty "
            + "WHERE p.id = :id AND p.stockQuantity >= :qty")
    int decreaseStock(@Param("id") Integer id, @Param("qty") int qty);
}
