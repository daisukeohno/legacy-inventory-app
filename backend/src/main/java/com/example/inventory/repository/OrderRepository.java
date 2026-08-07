package com.example.inventory.repository;

import com.example.inventory.domain.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items JOIN FETCH o.customer ORDER BY o.id DESC")
    List<Order> findAllWithItems();
}
