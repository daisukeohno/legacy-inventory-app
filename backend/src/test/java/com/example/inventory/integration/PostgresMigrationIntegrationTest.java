package com.example.inventory.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.inventory.domain.Order;
import com.example.inventory.repository.CustomerRepository;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.service.InsufficientStockException;
import com.example.inventory.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * クリーンなPostgreSQLに対する Flyway マイグレーション + アトミック在庫更新 + 全ロールバックの結合テスト。
 * Dockerが利用できない環境ではスキップされる。
 */
@SpringBootTest
@ActiveProfiles("postgres")
@Testcontainers(disabledWithoutDocker = true)
class PostgresMigrationIntegrationTest {

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
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void customerForeignKeyMigrationBackfillsEveryOrder() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        Integer nullCustomerIds = jdbc.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE customer_id IS NULL", Integer.class);
        assertThat(nullCustomerIds).isZero();

        String seededCustomer = jdbc.queryForObject(
                "SELECT c.name FROM orders o JOIN customer c ON c.id = o.customer_id ORDER BY o.id LIMIT 1",
                String.class);
        assertThat(seededCustomer).isEqualTo("株式会社サンプル商事");

        Integer fkCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints "
                        + "WHERE constraint_name = 'orders_customer_fk' AND constraint_type = 'FOREIGN KEY'",
                Integer.class);
        assertThat(fkCount).isEqualTo(1);
    }

    @Test
    void seededOrderTotalUsesScaleZero() {
        List<Order> orders = orderService.findAll();

        assertThat(orders).isNotEmpty();
        assertThat(orders.get(orders.size() - 1).getTotalAmount()).isEqualTo(new BigDecimal("261600"));
    }

    @Test
    void insufficientStockRollsBackEveryStockDeduction() {
        long customerId = customerRepository.findAll().get(0).getId();
        long laptopId = productRepository.findBySku("SKU-1001").orElseThrow().getId();
        long hubId = productRepository.findBySku("SKU-1003").orElseThrow().getId();
        int laptopStockBefore = productRepository.findById(laptopId).orElseThrow().getStockQuantity();
        long ordersBefore = orderRepository.count();

        assertThatThrownBy(() -> orderService.placeOrder(customerId, List.of(
                new OrderService.OrderLine(laptopId, 1),
                new OrderService.OrderLine(hubId, 999))))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(productRepository.findById(laptopId).orElseThrow().getStockQuantity())
                .isEqualTo(laptopStockBefore);
        assertThat(orderRepository.count()).isEqualTo(ordersBefore);
    }
}
