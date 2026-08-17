package com.example.inventory.service;

import com.example.inventory.domain.Order;
import com.example.inventory.domain.OrderItem;
import com.example.inventory.domain.Product;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.web.dto.OrderCreateRequest;
import com.example.inventory.web.dto.OrderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final DateTimeFormatter ORDER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String INITIAL_STATUS = "NEW";

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final Clock clock;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, Clock clock) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        return orderRepository.findAllByOrderByIdDesc().stream().map(OrderDto::of).toList();
    }

    /**
     * 在庫チェック→在庫引き落とし→注文保存を単一トランザクションで実行する。
     * 旧実装（OrderSaveAction + OrderDao/ProductDao）はコネクションのオートコミット任せで、
     * 途中で失敗すると注文だけ残る不整合があった。
     */
    @Transactional
    public OrderDto createOrder(OrderCreateRequest request) {
        Order order = new Order(
                request.customerName().trim(),
                LocalDate.now(clock).format(ORDER_DATE_FORMAT),
                INITIAL_STATUS
        );

        for (OrderCreateRequest.Item requested : request.items()) {
            Integer quantity = requested.quantity();
            if (quantity == null || quantity <= 0) {
                continue;
            }
            Optional<Product> found = productRepository.findById(requested.productId());
            if (found.isEmpty()) {
                log.warn("Ignoring order line for unknown product id={}", requested.productId());
                continue;
            }
            Product product = found.get();

            int updated = productRepository.decreaseStock(product.getId(), quantity);
            if (updated == 0) {
                throw new InsufficientStockException(
                        product.getId(), product.getName(), product.getStockQuantity(), quantity);
            }

            order.addItem(new OrderItem(product.getId(), product.getName(), product.getPrice(), quantity));
        }

        if (order.getItems().isEmpty()) {
            throw new EmptyOrderException();
        }

        Order saved = orderRepository.save(order);
        log.info("Created order id={} customer={} total={}",
                saved.getId(), saved.getCustomerName(), saved.getTotalAmount());
        return OrderDto.of(saved);
    }
}
