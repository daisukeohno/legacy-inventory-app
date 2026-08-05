package com.example.inventory.service;

import com.example.inventory.dto.CreateOrderRequest;
import com.example.inventory.dto.OrderDto;
import com.example.inventory.entity.Order;
import com.example.inventory.entity.OrderItem;
import com.example.inventory.entity.Product;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final DateTimeFormatter ORDER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String STATUS_NEW = "NEW";

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<OrderDto> findAll() {
        return orderRepository.findAllByOrderByIdDesc().stream()
                .map(OrderDto::from)
                .toList();
    }

    /**
     * 注文を確定する。数量が未入力/0以下の明細はスキップし、在庫引き落としと注文保存を
     * 同一トランザクションで行うため、在庫不足時は在庫も注文も更新されない。
     */
    @Transactional
    public OrderDto create(CreateOrderRequest request) {
        Order order = new Order(request.customerName().trim(),
                LocalDate.now().format(ORDER_DATE_FORMAT), STATUS_NEW);

        for (CreateOrderRequest.OrderItemInput input : request.items()) {
            if (input == null || input.productId() == null
                    || input.quantity() == null || input.quantity() <= 0) {
                continue;
            }
            Optional<Product> found = productRepository.findById(input.productId());
            if (found.isEmpty()) {
                continue;
            }
            Product product = found.get();
            int updated = productRepository.decreaseStock(product.getId(), input.quantity());
            if (updated == 0) {
                throw new InsufficientStockException("「" + product.getName()
                        + "」の在庫が不足しています(在庫数: " + product.getStockQuantity() + ")。");
            }
            order.addItem(new OrderItem(product.getId(), product.getName(),
                    product.getPrice(), input.quantity()));
        }

        if (order.getItems().isEmpty()) {
            throw new InvalidOrderException("少なくとも1つの商品を数量1以上で選択してください。");
        }

        return OrderDto.from(orderRepository.save(order));
    }
}
