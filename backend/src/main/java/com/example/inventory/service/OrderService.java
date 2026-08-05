package com.example.inventory.service;

import com.example.inventory.domain.Customer;
import com.example.inventory.domain.Order;
import com.example.inventory.domain.OrderItem;
import com.example.inventory.domain.OrderStatus;
import com.example.inventory.domain.Product;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.repository.CustomerRepository;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAllWithItems();
    }

    /**
     * 注文作成。在庫引き落とし・注文保存を単一トランザクションで行い、
     * 1 明細でも在庫不足なら例外を投げて注文全体をロールバックする(旧実装からの意図的な仕様変更)。
     */
    @Transactional
    public Order placeOrder(String customerName, List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("少なくとも1つの商品を数量1以上で選択してください。");
        }
        // customer.name は UNIQUE。同名の新規得意先が同時に登録された場合は
        // 一意制約違反で注文全体がロールバックされる(重複行は作られない)。
        Customer customer = customerRepository.findByName(customerName.trim())
                .orElseGet(() -> customerRepository.save(new Customer(customerName.trim(), null)));

        Order order = new Order(customer, LocalDate.now(), OrderStatus.NEW);

        for (OrderLine line : lines) {
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new NotFoundException("商品が見つかりません: id=" + line.productId()));

            int updated = productRepository.decreaseStock(product.getId(), line.quantity());
            if (updated == 0) {
                log.warn("在庫不足のため注文をロールバックします: productId={}, requested={}",
                        product.getId(), line.quantity());
                throw new InsufficientStockException(
                        "「" + product.getName() + "」の在庫が不足しています(在庫数: "
                                + product.getStockQuantity() + ")。");
            }
            order.addItem(new OrderItem(product.getId(), product.getName(), product.getPrice(), line.quantity()));
        }
        return orderRepository.save(order);
    }

    public record OrderLine(Long productId, int quantity) {
    }
}
