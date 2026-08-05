package com.example.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.inventory.AbstractPostgresTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * V3 マイグレーション(customer 正規化)の検証。
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerMigrationTest extends AbstractPostgresTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void everyOrderHasCustomerId() {
        Integer nulls = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE customer_id IS NULL", Integer.class);
        assertThat(nulls).isZero();

        String nullable = jdbcTemplate.queryForObject(
                "SELECT is_nullable FROM information_schema.columns "
                        + "WHERE table_name = 'orders' AND column_name = 'customer_id'", String.class);
        assertThat(nullable).isEqualTo("NO");
    }

    @Test
    void existingOrderIsLinkedToExistingCustomerWithoutCreatingDuplicates() {
        String name = jdbcTemplate.queryForObject(
                "SELECT c.name FROM orders o JOIN customer c ON c.id = o.customer_id WHERE o.id = 1",
                String.class);
        assertThat(name).isEqualTo("株式会社サンプル商事");

        Integer duplicates = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer WHERE name = '株式会社サンプル商事'", Integer.class);
        assertThat(duplicates).isEqualTo(1);
    }

    @Test
    void customerNameIsUnique() {
        Integer uniques = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints "
                        + "WHERE table_name = 'customer' AND constraint_name = 'uk_customer_name' "
                        + "AND constraint_type = 'UNIQUE'", Integer.class);
        assertThat(uniques).isEqualTo(1);

        jdbcTemplate.update("INSERT INTO customer (name, email) VALUES ('一意制約テスト', NULL)");
        assertThat(org.assertj.core.api.Assertions.catchThrowable(() ->
                jdbcTemplate.update("INSERT INTO customer (name, email) VALUES ('一意制約テスト', NULL)")))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void foreignKeyConstraintExists() {
        Integer fks = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints "
                        + "WHERE table_name = 'orders' AND constraint_type = 'FOREIGN KEY'", Integer.class);
        assertThat(fks).isEqualTo(1);
    }
}
