package com.example.inventory.service;

import com.example.inventory.domain.Customer;
import com.example.inventory.domain.Order;
import com.example.inventory.domain.OrderItem;
import com.example.inventory.domain.OrderStatus;
import com.example.inventory.domain.Product;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.repository.CustomerRepository;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

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
     * Places an order, decrementing stock atomically per line. Any line that cannot be
     * satisfied aborts the whole order.
     */
    @Transactional
    public Order placeOrder(String customerName, List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("At least one order line with quantity >= 1 is required");
        }

        Customer customer = customerRepository.findByName(customerName)
                .orElseGet(() -> customerRepository.save(new Customer(customerName, null)));

        Order order = new Order(customer, LocalDate.now(), OrderStatus.NEW);

        for (OrderLine line : lines) {
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + line.productId()));

            int updated = productRepository.decreaseStock(product.getId(), line.quantity());
            if (updated == 0) {
                throw new InsufficientStockException(product.getId(), product.getName(), line.quantity());
            }

            order.addItem(new OrderItem(product.getId(), product.getName(), product.getPrice(), line.quantity()));
        }

        return orderRepository.save(order);
    }

    public record OrderLine(Long productId, int quantity) {
    }
}
