package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.inventory.domain.Order;
import com.example.inventory.domain.Product;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 旧 OrderSaveAction の業務ロジック(在庫チェック・引き落とし・合計計算・単一トランザクション)を検証する。
 * トランザクションのロールバックを確認するため、テスト自体は @Transactional にしない。
 */
@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Product newProduct(String price, int stock) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return productRepository.save(
                new Product("SKU-" + suffix, "テスト商品-" + suffix, new BigDecimal(price), stock));
    }

    @Test
    @DisplayName("注文作成: 在庫が引き落とされ、明細と合計金額が保存される")
    void createOrderDeductsStock() {
        Product laptop = newProduct("128000.00", 24);
        Product mouse = newProduct("2800.00", 6);

        Order created = orderService.createOrder("株式会社サンプル商事", List.of(
                new OrderService.OrderLine(laptop.getId(), 2),
                new OrderService.OrderLine(mouse.getId(), 2)));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo("NEW");
        assertThat(created.getOrderDate()).isEqualTo(LocalDate.now());
        assertThat(created.getItems()).hasSize(2);
        assertThat(created.getTotalAmount()).isEqualByComparingTo("261600.00");

        assertThat(productRepository.findById(laptop.getId()).orElseThrow().getStockQuantity()).isEqualTo(22);
        assertThat(productRepository.findById(mouse.getId()).orElseThrow().getStockQuantity()).isEqualTo(4);

        // 明細には注文時点の商品名・単価がスナップショットとして保持される
        assertThat(created.getItems().get(0).getProductName()).isEqualTo(laptop.getName());
        assertThat(created.getItems().get(0).getUnitPrice()).isEqualByComparingTo("128000.00");
    }

    @Test
    @DisplayName("在庫不足: 旧実装と同じエラーメッセージの例外を投げ、在庫も注文も全件ロールバックされる")
    void insufficientStockRollsBackEverything() {
        Product enough = newProduct("1000.00", 50);
        Product scarce = newProduct("500.00", 3);
        long orderCountBefore = orderRepository.count();

        assertThatThrownBy(() -> orderService.createOrder("得意先A", List.of(
                new OrderService.OrderLine(enough.getId(), 5),
                new OrderService.OrderLine(scarce.getId(), 4))))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("「" + scarce.getName() + "」の在庫が不足しています(在庫数: 3)。");

        assertThat(productRepository.findById(enough.getId()).orElseThrow().getStockQuantity()).isEqualTo(50);
        assertThat(productRepository.findById(scarce.getId()).orElseThrow().getStockQuantity()).isEqualTo(3);
        assertThat(orderRepository.count()).isEqualTo(orderCountBefore);
    }

    @Test
    @DisplayName("在庫と同数の注文は成功する(在庫チェックは stock >= quantity)")
    void orderingExactStockSucceeds() {
        Product product = newProduct("100.00", 5);

        Order created = orderService.createOrder("得意先B",
                List.of(new OrderService.OrderLine(product.getId(), 5)));

        assertThat(created.getTotalAmount()).isEqualByComparingTo("500.00");
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isZero();
    }

    @Test
    @DisplayName("得意先名が空の場合はエラー")
    void blankCustomerNameIsRejected() {
        Product product = newProduct("100.00", 5);

        assertThatThrownBy(() -> orderService.createOrder("  ",
                List.of(new OrderService.OrderLine(product.getId(), 1))))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessage("得意先名を入力してください。");
    }

    @Test
    @DisplayName("数量0以下の行は明細に含めず、有効な明細が無ければエラー")
    void nonPositiveQuantitiesAreIgnored() {
        Product product = newProduct("100.00", 5);

        assertThatThrownBy(() -> orderService.createOrder("得意先C", List.of(
                new OrderService.OrderLine(product.getId(), 0))))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessage("少なくとも1つの商品を数量1以上で選択してください。");

        assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("明細が空の場合はエラー")
    void emptyItemsAreRejected() {
        assertThatThrownBy(() -> orderService.createOrder("得意先D", List.of()))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessage("少なくとも1つの商品を数量1以上で選択してください。");
    }

    @Test
    @DisplayName("存在しない商品IDは404相当の例外")
    void unknownProductIsRejected() {
        assertThatThrownBy(() -> orderService.createOrder("得意先E",
                List.of(new OrderService.OrderLine(999999L, 1))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("注文一覧はID降順で明細付きで取得できる")
    void findAllReturnsOrdersWithItems() {
        Product product = newProduct("100.00", 20);
        Order first = orderService.createOrder("得意先F",
                List.of(new OrderService.OrderLine(product.getId(), 1)));

        List<Order> orders = orderService.findAll();

        assertThat(orders.get(0).getId()).isEqualTo(first.getId());
        assertThat(orders.get(0).getItems()).hasSize(1);
        assertThat(orders).isSortedAccordingTo((a, b) -> Long.compare(b.getId(), a.getId()));
    }
}
