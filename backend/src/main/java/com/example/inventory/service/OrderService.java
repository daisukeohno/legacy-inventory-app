package com.example.inventory.service;

import com.example.inventory.domain.Order;
import com.example.inventory.domain.OrderItem;
import com.example.inventory.domain.Product;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 旧 OrderSaveAction / OrderListAction の業務ロジックを集約したサービス。
 *
 * 旧実装では注文保存(OrderDao)と在庫引き落とし(ProductDao)が別コネクション・別トランザクション
 * だったが、ここでは createOrder 全体を単一トランザクションにして不整合を防ぐ。
 * 在庫チェック条件・エラーメッセージは旧実装のままとする。
 */
@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<Order> findAll() {
        return orderRepository.findAllByOrderByIdDesc();
    }

    /**
     * 注文を作成する。得意先名の必須チェック → 明細ごとの在庫チェック → 在庫引き落とし →
     * 注文と明細の保存 を1トランザクションで行う。在庫不足時は例外を投げて全件ロールバックする。
     */
    @Transactional
    public Order createOrder(String customerName, List<OrderLine> lines) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new InvalidOrderException("得意先名を入力してください。");
        }

        Order order = new Order(customerName.trim(), LocalDate.now());

        for (OrderLine line : lines == null ? List.<OrderLine>of() : lines) {
            // 旧実装と同様、数量が未入力/0以下の行は明細に含めない
            if (line == null || line.quantity() <= 0) {
                continue;
            }
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "商品が見つかりません(id: " + line.productId() + ")。"));

            if (!product.hasEnoughStock(line.quantity())) {
                throw new InsufficientStockException(
                        "「" + product.getName() + "」の在庫が不足しています(在庫数: "
                                + product.getStockQuantity() + ")。");
            }
            product.decreaseStock(line.quantity());
            order.addItem(new OrderItem(product.getId(), product.getName(), product.getPrice(),
                    line.quantity()));
        }

        if (order.getItems().isEmpty()) {
            throw new InvalidOrderException("少なくとも1つの商品を数量1以上で選択してください。");
        }

        Order saved = orderRepository.save(order);
        log.info("order created: id={}, customer={}, items={}, total={}",
                saved.getId(), saved.getCustomerName(), saved.getItems().size(), saved.getTotalAmount());
        return saved;
    }

    /** 注文明細の入力値(商品ID + 数量)。旧 OrderForm の productIds[]/quantities[] 並行配列を置き換える。 */
    public record OrderLine(Long productId, int quantity) {
    }
}
