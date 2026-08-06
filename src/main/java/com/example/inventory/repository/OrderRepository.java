package com.example.inventory.repository;

import com.example.inventory.domain.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByOrderByIdDesc();
}
