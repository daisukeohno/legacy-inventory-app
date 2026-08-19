package com.example.inventory.repository;

import com.example.inventory.domain.Order;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /** 旧 OrderDao.findAll() 相当。明細をJOIN FETCHしてN+1クエリを回避する。 */
    @EntityGraph(attributePaths = "items")
    List<Order> findAllByOrderByIdDesc();
}
