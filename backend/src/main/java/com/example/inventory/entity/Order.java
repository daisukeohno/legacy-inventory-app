package com.example.inventory.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(name = "order_date", nullable = false, length = 20)
    private String orderDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    @OrderBy("id")
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {
    }

    public Order(String customerName, String orderDate, String status) {
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.status = status;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Integer getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
