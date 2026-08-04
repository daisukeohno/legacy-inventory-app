package com.example.legacy.listener;

import com.example.legacy.dao.DbUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Connection;
import java.sql.Statement;

/**
 * アプリ起動時に埋め込みH2DBへスキーマ作成 + 初期データ投入を行う。
 * 本番相当のDBスキーマ管理(Flyway/Liquibase等)は導入されておらず、
 * DDLがJavaコードにベタ書きされているのもレガシーの典型例。
 */
public class DbInitListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        Connection con = null;
        try {
            con = DbUtil.getConnection();
            Statement st = con.createStatement();

            st.execute("CREATE TABLE product ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "sku VARCHAR(50) NOT NULL,"
                    + "name VARCHAR(200) NOT NULL,"
                    + "price DOUBLE NOT NULL,"
                    + "stock_quantity INT NOT NULL)");

            st.execute("CREATE TABLE customer ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "name VARCHAR(200) NOT NULL,"
                    + "email VARCHAR(200))");

            st.execute("CREATE TABLE orders ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "customer_name VARCHAR(200) NOT NULL,"
                    + "order_date VARCHAR(20) NOT NULL,"
                    + "status VARCHAR(20) NOT NULL)");

            st.execute("CREATE TABLE order_item ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "order_id INT NOT NULL,"
                    + "product_id INT NOT NULL,"
                    + "product_name VARCHAR(200) NOT NULL,"
                    + "unit_price DOUBLE NOT NULL,"
                    + "quantity INT NOT NULL)");

            st.execute("INSERT INTO product (sku, name, price, stock_quantity) VALUES "
                    + "('SKU-1001', 'ノートPC 14インチ', 128000, 24),"
                    + "('SKU-1002', 'ワイヤレスマウス', 2800, 6),"
                    + "('SKU-1003', 'USB-Cハブ (7in1)', 4500, 3),"
                    + "('SKU-1004', '外付けSSD 1TB', 15800, 40),"
                    + "('SKU-1005', 'モニター 27インチ 4K', 46000, 8),"
                    + "('SKU-1006', 'メカニカルキーボード', 9800, 15)");

            st.execute("INSERT INTO customer (name, email) VALUES "
                    + "('株式会社サンプル商事', 'order@sample-shoji.example.co.jp'),"
                    + "('合同会社デモロジスティクス', 'contact@demo-logistics.example.co.jp')");

            st.execute("INSERT INTO orders (customer_name, order_date, status) VALUES "
                    + "('株式会社サンプル商事', '2026-07-20', 'SHIPPED')");

            st.execute("INSERT INTO order_item (order_id, product_id, product_name, unit_price, quantity) VALUES "
                    + "(1, 1, 'ノートPC 14インチ', 128000, 2),"
                    + "(1, 2, 'ワイヤレスマウス', 2800, 2)");

            System.out.println("[DbInitListener] H2 in-memory DB initialized with seed data.");
        } catch (Exception e) {
            System.out.println("[DbInitListener] DB init failed: " + e.getMessage());
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // 特に何もしない(レガシー実装ではリソース解放処理も省略されがち)
    }
}
