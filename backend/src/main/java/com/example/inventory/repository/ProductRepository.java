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
     * keyword is matched as a substring; pass an empty string to match everything.
     */
    @Query("""
            SELECT p FROM Product p
            WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:lowStock = false OR p.stockQuantity < :threshold)
            ORDER BY p.id
            """)
    List<Product> search(@Param("keyword") String keyword,
                         @Param("lowStock") boolean lowStock,
                         @Param("threshold") int threshold);

    /**
     * Conditional atomic decrement; returns 0 when stock is insufficient.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity
            WHERE p.id = :id AND p.stockQuantity >= :quantity
            """)
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);
}
