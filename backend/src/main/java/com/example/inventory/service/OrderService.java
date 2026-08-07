package com.example.inventory.service;

import com.example.inventory.domain.Customer;
import com.example.inventory.domain.Order;
import com.example.inventory.domain.OrderItem;
import com.example.inventory.domain.OrderStatus;
import com.example.inventory.domain.Product;
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
     * 旧 OrderSaveAction の業務ロジックを集約した注文確定処理。
     * 在庫不足時は例外を送出し、引き落とし済みの在庫も含めて全ロールバックする。
     */
    @Transactional
    public Order placeOrder(long customerId, List<OrderLine> lines) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("得意先が見つかりません (id=" + customerId + ")"));

        Order order = new Order(customer, LocalDate.now(), OrderStatus.NEW);

        for (OrderLine line : lines) {
            if (line.quantity() <= 0) {
                continue;
            }
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new NotFoundException("商品が見つかりません (id=" + line.productId() + ")"));

            int updated = productRepository.decreaseStock(product.getId(), line.quantity());
            if (updated == 0) {
                log.warn("在庫不足のため注文を中止します productId={} requested={} stock={}",
                        product.getId(), line.quantity(), product.getStockQuantity());
                throw new InsufficientStockException("「" + product.getName() + "」の在庫が不足しています(在庫数: "
                        + product.getStockQuantity() + ")。");
            }

            order.addItem(new OrderItem(product, line.quantity()));
        }

        if (order.getItems().isEmpty()) {
            throw new InvalidOrderException("少なくとも1つの商品を数量1以上で選択してください。");
        }

        return orderRepository.save(order);
    }

    public record OrderLine(long productId, int quantity) {
    }
}
