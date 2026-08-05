package com.example.inventory.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventory.config.AppProperties;
import com.example.inventory.domain.Customer;
import com.example.inventory.domain.Order;
import com.example.inventory.domain.OrderItem;
import com.example.inventory.domain.OrderStatus;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.service.OrderService;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@EnableConfigurationProperties(AppProperties.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    OrderService orderService;

    private static Order sampleOrder() throws Exception {
        Customer customer = new Customer("株式会社サンプル商事", null);
        setId(customer, 1L);
        Order order = new Order(customer, LocalDate.of(2026, 7, 20), OrderStatus.NEW);
        setId(order, 10L);
        order.addItem(new OrderItem(1L, "ノートPC 14インチ", new BigDecimal("128000"), 2));
        return order;
    }

    private static void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }

    @Test
    void listReturnsOrdersWithNumericAmounts() throws Exception {
        given(orderService.findAll()).willReturn(List.of(sampleOrder()));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerName").value("株式会社サンプル商事"))
                .andExpect(jsonPath("$[0].orderDate").value("2026-07-20"))
                .andExpect(jsonPath("$[0].status").value("NEW"))
                .andExpect(jsonPath("$[0].items[0].subtotal").value(256000))
                .andExpect(jsonPath("$[0].totalAmount").value(256000));
    }

    @Test
    void createReturns201() throws Exception {
        given(orderService.placeOrder(anyString(), any())).willReturn(sampleOrder());

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"株式会社サンプル商事","items":[{"productId":1,"quantity":2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void insufficientStockReturns409() throws Exception {
        willThrow(new InsufficientStockException("「USB-Cハブ (7in1)」の在庫が不足しています(在庫数: 3)。"))
                .given(orderService).placeOrder(anyString(), any());

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"得意先","items":[{"productId":3,"quantity":999}]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void emptyItemsReturns400() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"得意先","items":[]}
                                """))
                .andExpect(status().isBadRequest());
    }
}
