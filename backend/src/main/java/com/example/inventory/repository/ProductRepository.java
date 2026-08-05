package com.example.inventory.repository;

import com.example.inventory.domain.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 商品検索。keyword は正規化済み(null 不可、空文字は全件)を前提とする。
     * threshold 未満の在庫だけに絞りたくない場合は Integer.MAX_VALUE を渡す。
     */
    @Query("""
            SELECT p FROM Product p
            WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND p.stockQuantity < :threshold
            ORDER BY p.id
            """)
    List<Product> search(@Param("keyword") String keyword, @Param("threshold") int threshold);

    /**
     * 在庫のアトミック引き落とし。楽観/悲観ロックを使わず、条件付き UPDATE の更新件数で
     * 在庫の充足を判定する。戻り値 0 は在庫不足を意味する。
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE product SET stock_quantity = stock_quantity - :qty "
            + "WHERE id = :id AND stock_quantity >= :qty", nativeQuery = true)
    int decreaseStock(@Param("id") Long id, @Param("qty") int quantity);
}
