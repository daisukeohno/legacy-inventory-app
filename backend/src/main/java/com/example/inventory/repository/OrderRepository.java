package com.example.inventory.repository;

import com.example.inventory.domain.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /** 旧 OrderDao.findAll と同じ id 降順。明細は N+1 を避けるため EntityGraph で同時取得する。 */
    @EntityGraph(attributePaths = "items")
    List<Order> findAllByOrderByIdDesc();
}
