package com.example.inventory.repository;

import com.example.inventory.domain.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByIdAsc();

    List<Product> findByStockQuantityLessThanOrderByIdAsc(int threshold);

    List<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCaseOrderByIdAsc(String name, String sku);

    /**
     * 旧 ProductDao.search() の「全件取得 + Java 側フィルタ」をDB側のクエリに置き換えたもの。
     */
    @Query("""
            select p from Product p
            where (lower(p.name) like lower(concat('%', :keyword, '%'))
                or lower(p.sku) like lower(concat('%', :keyword, '%')))
              and p.stockQuantity < :threshold
            order by p.id asc
            """)
    List<Product> searchLowStock(@Param("keyword") String keyword,
                                 @Param("threshold") int threshold);
}
