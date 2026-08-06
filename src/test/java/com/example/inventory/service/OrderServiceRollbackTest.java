package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.inventory.dto.OrderItemRequest;
import com.example.inventory.dto.OrderRequest;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 在庫引き落とし → 注文保存が1トランザクションであることを、テスト側のトランザクションを
 * 張らずに検証する(在庫不足時は先行明細の引き落としもロールバックされる)。
 */
@SpringBootTest
class OrderServiceRollbackTest {

    private static final int LAPTOP_ID = 1;      // SKU-1001 在庫 24
    private static final int USB_HUB_ID = 3;     // SKU-1003 在庫 3

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void insufficientStockRollsBackEarlierStockDeductionsAndOrder() {
        int laptopStockBefore = productRepository.findById(LAPTOP_ID).orElseThrow().getStockQuantity();
        int ordersBefore = orderRepository.findAllByOrderByIdDesc().size();

        assertThatThrownBy(() -> orderService.createOrder(new OrderRequest("得意先", List.of(
                new OrderItemRequest(LAPTOP_ID, 1),
                new OrderItemRequest(USB_HUB_ID, 99)))))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(productRepository.findById(LAPTOP_ID).orElseThrow().getStockQuantity())
                .isEqualTo(laptopStockBefore);
        assertThat(orderRepository.findAllByOrderByIdDesc()).hasSize(ordersBefore);
    }
}
