package com.example.legacy.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Order implements Serializable {

    private int id;
    private String customerName;
    private String orderDate;   // 本来はDate型にすべきだが、レガシー実装では文字列で保持
    private String status;      // "NEW" / "SHIPPED" などをただのStringで管理
    private List<OrderItem> items = new ArrayList<OrderItem>();

    public Order() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    public double getTotalAmount() {
        double total = 0.0;
        for (int i = 0; i < items.size(); i++) {
            total += items.get(i).getSubtotal();
        }
        return total;
    }
}
