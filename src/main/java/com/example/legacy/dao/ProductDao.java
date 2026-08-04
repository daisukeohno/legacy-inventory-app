package com.example.legacy.dao;

import com.example.legacy.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品(在庫)DAO。ORMを使わず、生JDBCでSQLを直接組み立てている。
 * サービス層が存在せず、業務ロジック(在庫引き落とし等)もこのクラスから直接呼ばれる。
 */
public class ProductDao {

    public List<Product> findAll() {
        List<Product> result = new ArrayList<Product>();
        String sql = "SELECT id, sku, name, price, stock_quantity FROM product ORDER BY id";
        Connection con = null;
        try {
            con = DbUtil.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            // レガシー実装: 例外を握りつぶしてスタックトレースを標準出力に出すだけ
            System.out.println("[ProductDao] findAll failed: " + e.getMessage());
        } finally {
            close(con);
        }
        return result;
    }

    public List<Product> search(String keyword, boolean lowStockOnly) {
        List<Product> all = findAll();
        List<Product> filtered = new ArrayList<Product>();
        for (int i = 0; i < all.size(); i++) {
            Product p = all.get(i);
            boolean matchesKeyword = (keyword == null || keyword.trim().length() == 0)
                    || p.getName().toLowerCase().contains(keyword.toLowerCase())
                    || p.getSku().toLowerCase().contains(keyword.toLowerCase());
            boolean matchesStock = (!lowStockOnly) || p.isLowStock();
            if (matchesKeyword && matchesStock) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    public Product findById(int id) {
        String sql = "SELECT id, sku, name, price, stock_quantity FROM product WHERE id = ?";
        Connection con = null;
        try {
            con = DbUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.out.println("[ProductDao] findById failed: " + e.getMessage());
        } finally {
            close(con);
        }
        return null;
    }

    public void save(Product product) {
        if (product.getId() == 0) {
            insert(product);
        } else {
            update(product);
        }
    }

    private void insert(Product product) {
        String sql = "INSERT INTO product (sku, name, price, stock_quantity) VALUES (?, ?, ?, ?)";
        Connection con = null;
        try {
            con = DbUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, product.getSku());
            ps.setString(2, product.getName());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getStockQuantity());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                product.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println("[ProductDao] insert failed: " + e.getMessage());
        } finally {
            close(con);
        }
    }

    private void update(Product product) {
        String sql = "UPDATE product SET sku = ?, name = ?, price = ?, stock_quantity = ? WHERE id = ?";
        Connection con = null;
        try {
            con = DbUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, product.getSku());
            ps.setString(2, product.getName());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getStockQuantity());
            ps.setInt(5, product.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[ProductDao] update failed: " + e.getMessage());
        } finally {
            close(con);
        }
    }

    /**
     * 在庫を引き落とす。呼び出し元(OrderSaveAction)から直接呼ばれ、
     * トランザクション境界も明示的に管理されていない。
     */
    public boolean decreaseStock(int productId, int quantity) {
        String sql = "UPDATE product SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";
        Connection con = null;
        try {
            con = DbUtil.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.out.println("[ProductDao] decreaseStock failed: " + e.getMessage());
            return false;
        } finally {
            close(con);
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("sku"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getInt("stock_quantity")
        );
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
