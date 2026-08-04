package com.example.legacy.dao;

import com.example.legacy.model.Order;
import com.example.legacy.model.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<Order>();
        String sql = "SELECT id, customer_name, order_date, status FROM orders ORDER BY id DESC";
        Connection con = null;
        try {
            con = DbUtil.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setCustomerName(rs.getString("customer_name"));
                order.setOrderDate(rs.getString("order_date"));
                order.setStatus(rs.getString("status"));
                order.setItems(findItems(con, order.getId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.out.println("[OrderDao] findAll failed: " + e.getMessage());
        } finally {
            close(con);
        }
        return orders;
    }

    private List<OrderItem> findItems(Connection con, int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<OrderItem>();
        String sql = "SELECT product_id, product_name, unit_price, quantity FROM order_item WHERE order_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, orderId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            items.add(new OrderItem(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getDouble("unit_price"),
                    rs.getInt("quantity")
            ));
        }
        return items;
    }

    /**
     * 注文とその明細をまとめて保存する。
     * 本来はここで1トランザクションにすべきだが、レガシー実装ではコネクションの
     * オートコミットに任せてしまっており、途中で失敗すると注文だけ残るリスクがある。
     */
    public void save(Order order) {
        Connection con = null;
        try {
            con = DbUtil.getConnection();

            String insertOrderSql = "INSERT INTO orders (customer_name, order_date, status) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, order.getCustomerName());
            ps.setString(2, order.getOrderDate());
            ps.setString(3, order.getStatus());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int orderId = 0;
            if (keys.next()) {
                orderId = keys.getInt(1);
                order.setId(orderId);
            }

            String insertItemSql = "INSERT INTO order_item (order_id, product_id, product_name, unit_price, quantity) "
                    + "VALUES (?, ?, ?, ?, ?)";
            for (int i = 0; i < order.getItems().size(); i++) {
                OrderItem item = order.getItems().get(i);
                PreparedStatement itemPs = con.prepareStatement(insertItemSql);
                itemPs.setInt(1, orderId);
                itemPs.setInt(2, item.getProductId());
                itemPs.setString(3, item.getProductName());
                itemPs.setDouble(4, item.getUnitPrice());
                itemPs.setInt(5, item.getQuantity());
                itemPs.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("[OrderDao] save failed: " + e.getMessage());
        } finally {
            close(con);
        }
    }

    private void close(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }
}
