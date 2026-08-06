package com.example.inventory.service;

import com.example.inventory.domain.Order;
import com.example.inventory.domain.OrderItem;
import com.example.inventory.domain.Product;
import com.example.inventory.dto.OrderItemRequest;
import com.example.inventory.dto.OrderRequest;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String STATUS_NEW = "NEW";

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final Clock clock;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, Clock clock) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAllByOrderByIdDesc();
    }

    /**
     * 旧 OrderSaveAction のフローを1トランザクションで再現する。
     * 数量が0以下/未入力の明細はスキップ、存在しない商品はスキップ、在庫不足は業務例外(ロールバック)、
     * 有効な明細が0件なら業務例外。在庫引き落とし後に注文を保存する。
     */
    @Transactional
    public Order createOrder(OrderRequest request) {
        Order order = new Order(request.customerName().trim(), LocalDate.now(clock), STATUS_NEW);

        for (OrderItemRequest line : request.items()) {
            Integer quantity = line.quantity();
            if (quantity == null || quantity <= 0) {
                continue;
            }
            Product product = productRepository.findById(line.productId()).orElse(null);
            if (product == null) {
                log.warn("Skipped order line for unknown product id={}", line.productId());
                continue;
            }
            String productName = product.getName();
            int availableQuantity = product.getStockQuantity();
            var unitPrice = product.getPrice();

            if (productRepository.decreaseStock(product.getId(), quantity) == 0) {
                log.info("Insufficient stock for product id={} requested={} available={}",
                        product.getId(), quantity, availableQuantity);
                throw new InsufficientStockException(productName, availableQuantity);
            }
            order.addItem(new OrderItem(product.getId(), productName, unitPrice, quantity));
        }

        if (order.getItems().isEmpty()) {
            throw new EmptyOrderException();
        }

        Order saved = orderRepository.save(order);
        log.info("Created order id={} customer={} total={}",
                saved.getId(), saved.getCustomerName(), saved.getTotalAmount());
        return saved;
    }
}
