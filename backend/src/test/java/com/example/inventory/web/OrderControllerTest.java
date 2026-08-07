package com.example.inventory.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventory.domain.Customer;
import com.example.inventory.domain.Order;
import com.example.inventory.domain.OrderItem;
import com.example.inventory.domain.OrderStatus;
import com.example.inventory.domain.Product;
import com.example.inventory.service.InsufficientStockException;
import com.example.inventory.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    private Order sampleOrder() {
        Order order = new Order(new Customer("株式会社サンプル商事", "order@example.co.jp"),
                LocalDate.of(2026, 7, 20), OrderStatus.NEW);
        order.addItem(new OrderItem(new Product("SKU-1001", "ノートPC 14インチ", new BigDecimal("128000"), 24), 2));
        return order;
    }

    @Test
    void listReturnsOrdersWithTotalAmount() throws Exception {
        given(orderService.findAll()).willReturn(List.of(sampleOrder()));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customer.name").value("株式会社サンプル商事"))
                .andExpect(jsonPath("$[0].orderDate").value("2026-07-20"))
                .andExpect(jsonPath("$[0].status").value("NEW"))
                .andExpect(jsonPath("$[0].items[0].subtotal").value(256000))
                .andExpect(jsonPath("$[0].totalAmount").value(256000));
    }

    @Test
    void createReturns201() throws Exception {
        given(orderService.placeOrder(anyLong(), any())).willReturn(sampleOrder());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":1,\"items\":[{\"productId\":1,\"quantity\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(256000));
    }

    @Test
    void insufficientStockReturns409() throws Exception {
        given(orderService.placeOrder(anyLong(), any()))
                .willThrow(new InsufficientStockException("「ワイヤレスマウス」の在庫が不足しています(在庫数: 1)。"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":1,\"items\":[{\"productId\":2,\"quantity\":5}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("「ワイヤレスマウス」の在庫が不足しています(在庫数: 1)。"));
    }

    @Test
    void emptyItemsAreRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":1,\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingCustomerIdIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1,\"quantity\":1}]}"))
                .andExpect(status().isBadRequest());
    }
}
