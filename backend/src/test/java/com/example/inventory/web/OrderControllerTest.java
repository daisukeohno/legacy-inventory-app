package com.example.inventory.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventory.domain.Order;
import com.example.inventory.domain.OrderItem;
import com.example.inventory.service.InsufficientStockException;
import com.example.inventory.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    private static Order sampleOrder() {
        Order order = new Order("株式会社サンプル商事", LocalDate.of(2026, 7, 20));
        order.addItem(new OrderItem(1L, "ノートPC 14インチ", new BigDecimal("128000.00"), 2));
        order.addItem(new OrderItem(2L, "ワイヤレスマウス", new BigDecimal("2800.00"), 2));
        return order;
    }

    @Test
    @DisplayName("GET /api/orders は明細と合計金額を含む注文一覧を返す")
    void listOrders() throws Exception {
        given(orderService.findAll()).willReturn(List.of(sampleOrder()));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerName").value("株式会社サンプル商事"))
                .andExpect(jsonPath("$[0].orderDate").value("2026-07-20"))
                .andExpect(jsonPath("$[0].items.length()").value(2))
                .andExpect(jsonPath("$[0].items[0].subtotal").value(256000.00))
                .andExpect(jsonPath("$[0].totalAmount").value(261600.00));
    }

    @Test
    @DisplayName("POST /api/orders は201で作成した注文を返す")
    void createOrder() throws Exception {
        given(orderService.createOrder(anyString(), any())).willReturn(sampleOrder());

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"株式会社サンプル商事",
                                 "items":[{"productId":1,"quantity":2},{"productId":2,"quantity":2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.totalAmount").value(261600.00));
    }

    @Test
    @DisplayName("在庫不足は409 + 旧実装と同じメッセージ")
    void createOrderInsufficientStock() throws Exception {
        willThrow(new InsufficientStockException("「ワイヤレスマウス」の在庫が不足しています(在庫数: 6)。"))
                .given(orderService).createOrder(anyString(), any());

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"得意先","items":[{"productId":2,"quantity":100}]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.message").value("「ワイヤレスマウス」の在庫が不足しています(在庫数: 6)。"));
    }

    @Test
    @DisplayName("得意先名なし・明細なしは400")
    void createOrderValidationError() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"","items":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[*].message",
                        org.hamcrest.Matchers.hasItems("得意先名を入力してください。",
                                "少なくとも1つの商品を数量1以上で選択してください。")));
    }
}
