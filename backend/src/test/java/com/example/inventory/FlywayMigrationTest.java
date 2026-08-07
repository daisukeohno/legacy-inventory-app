package com.example.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class FlywayMigrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Test
    void migrationsApplyCleanly() throws Exception {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("2");

        try (Connection con = dataSource.getConnection(); Statement st = con.createStatement()) {
            ResultSet columns = st.executeQuery("""
                    SELECT column_name, is_nullable FROM information_schema.columns
                    WHERE table_name = 'orders'
                    """);
            boolean sawCustomerId = false;
            boolean sawCustomerName = false;
            while (columns.next()) {
                String name = columns.getString("column_name");
                if ("customer_id".equals(name)) {
                    sawCustomerId = true;
                    assertThat(columns.getString("is_nullable")).isEqualTo("NO");
                }
                if ("customer_name".equals(name)) {
                    sawCustomerName = true;
                }
            }
            assertThat(sawCustomerId).isTrue();
            assertThat(sawCustomerName).isFalse();

            ResultSet fk = st.executeQuery("""
                    SELECT COUNT(*) AS c FROM information_schema.table_constraints
                    WHERE table_name = 'orders' AND constraint_type = 'FOREIGN KEY'
                      AND constraint_name = 'fk_orders_customer'
                    """);
            fk.next();
            assertThat(fk.getInt("c")).isEqualTo(1);

            ResultSet seeded = st.executeQuery("""
                    SELECT c.name, COUNT(oi.id) AS items
                    FROM orders o
                    JOIN customer c ON c.id = o.customer_id
                    JOIN order_item oi ON oi.order_id = o.id
                    GROUP BY c.name
                    """);
            seeded.next();
            assertThat(seeded.getString("name")).isEqualTo("株式会社サンプル商事");
            assertThat(seeded.getInt("items")).isEqualTo(2);
            assertThat(seeded.next()).isFalse();

            ResultSet products = st.executeQuery("SELECT COUNT(*) AS c FROM product");
            products.next();
            assertThat(products.getInt("c")).isEqualTo(6);
        }
    }
}
