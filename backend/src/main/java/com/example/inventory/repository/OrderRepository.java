package com.example.inventory.repository;

import com.example.inventory.entity.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findAllByOrderByIdDesc();
}
