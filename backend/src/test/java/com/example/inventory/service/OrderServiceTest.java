package com.example.inventory.service;

import com.example.inventory.domain.Order;
import com.example.inventory.domain.Product;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.web.dto.OrderCreateRequest;
import com.example.inventory.web.dto.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-08-17T02:00:00Z"), ZoneId.of("Asia/Tokyo"));

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, productRepository, FIXED_CLOCK);
    }

    private Product product(long id, String name, String price, int stock) {
        Product product = new Product("SKU-" + id, name, new BigDecimal(price), stock);
        setId(product, id);
        lenient().when(productRepository.findById(id)).thenReturn(Optional.of(product));
        return product;
    }

    private void setId(Object entity, long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void createOrderComputesSubtotalsAndTotal() {
        product(1L, "ノートPC 14インチ", "128000", 24);
        product(2L, "ワイヤレスマウス", "2800", 6);
        when(productRepository.decreaseStock(anyLong(), anyInt())).thenReturn(1);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderDto order = orderService.createOrder(new OrderCreateRequest("株式会社サンプル商事", List.of(
                new OrderCreateRequest.Item(1L, 2),
                new OrderCreateRequest.Item(2L, 3))));

        assertThat(order.orderDate()).isEqualTo("2026-08-17");
        assertThat(order.status()).isEqualTo("NEW");
        assertThat(order.items()).hasSize(2);
        assertThat(order.items().get(0).subtotal()).isEqualByComparingTo("256000");
        assertThat(order.items().get(1).subtotal()).isEqualByComparingTo("8400");
        assertThat(order.totalAmount()).isEqualByComparingTo("264400");
    }

    @Test
    void createOrderDecreasesStockForEachLine() {
        product(1L, "ノートPC 14インチ", "128000", 24);
        when(productRepository.decreaseStock(1L, 4)).thenReturn(1);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        orderService.createOrder(new OrderCreateRequest("得意先", List.of(
                new OrderCreateRequest.Item(1L, 4))));

        verify(productRepository).decreaseStock(1L, 4);
    }

    @Test
    void createOrderThrowsAndSkipsSaveWhenStockInsufficient() {
        product(3L, "USB-Cハブ (7in1)", "4500", 3);
        when(productRepository.decreaseStock(3L, 5)).thenReturn(0);

        assertThatThrownBy(() -> orderService.createOrder(new OrderCreateRequest("得意先", List.of(
                new OrderCreateRequest.Item(3L, 5)))))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("USB-Cハブ (7in1)")
                .hasMessageContaining("在庫数: 3");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrderIgnoresNonPositiveQuantitiesAndUnknownProducts() {
        product(1L, "ノートPC 14インチ", "128000", 24);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        when(productRepository.decreaseStock(1L, 1)).thenReturn(1);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderDto order = orderService.createOrder(new OrderCreateRequest("得意先", List.of(
                new OrderCreateRequest.Item(1L, 0),
                new OrderCreateRequest.Item(1L, null),
                new OrderCreateRequest.Item(999L, 2),
                new OrderCreateRequest.Item(1L, 1))));

        assertThat(order.items()).hasSize(1);
        verify(productRepository, never()).decreaseStock(999L, 2);
    }

    @Test
    void createOrderThrowsWhenNoValidItem() {
        assertThatThrownBy(() -> orderService.createOrder(new OrderCreateRequest("得意先", List.of(
                new OrderCreateRequest.Item(1L, 0)))))
                .isInstanceOf(EmptyOrderException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrderThrowsWhenItemsMissing() {
        assertThatThrownBy(() -> orderService.createOrder(new OrderCreateRequest("得意先", null)))
                .isInstanceOf(EmptyOrderException.class);
    }
}
