package com.example.legacy.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DB接続ユーティリティ。
 *
 * レガシー実装の典型的なアンチパターン:
 *  - 接続文字列・ユーザー/パスワードがソースにハードコード
 *  - コネクションプールを使わず都度 getConnection()
 *  - DataSource / JNDI ルックアップを使っていない
 *
 * Devinでのマイグレーション時は、Spring Boot + application.properties + HikariCP(デフォルト)
 * + Spring Data JPA へ置き換えることを想定。
 */
public final class DbUtil {

    private static final String URL = "jdbc:h2:mem:legacydb;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private DbUtil() {
    }

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
