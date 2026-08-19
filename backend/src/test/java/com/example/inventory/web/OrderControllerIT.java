package com.example.inventory.web;

import com.example.inventory.AbstractPostgresIntegrationTest;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class OrderControllerIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private String orderBody(String customerName, List<Map<String, Object>> items) throws Exception {
        return objectMapper.writeValueAsString(Map.of("customerName", customerName, "items", items));
    }

    @Test
    void listReturnsSeededOrderWithItemsAndTotal() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].customerName").value("株式会社サンプル商事"))
                .andExpect(jsonPath("$[0].orderDate").value("2026-07-20"))
                .andExpect(jsonPath("$[0].status").value("SHIPPED"))
                .andExpect(jsonPath("$[0].items", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].items[0].subtotal").value(256000))
                .andExpect(jsonPath("$[0].items[1].subtotal").value(5600))
                .andExpect(jsonPath("$[0].totalAmount").value(261600));
    }

    @Test
    void createOrderDeductsStockAndPersistsItems() throws Exception {
        String body = orderBody("新規得意先", List.of(
                Map.of("productId", 1, "quantity", 2),
                Map.of("productId", 2, "quantity", 3)));

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.items", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.totalAmount").value(264400));

        assertThat(productRepository.findById(1L).orElseThrow().getStockQuantity()).isEqualTo(22);
        assertThat(productRepository.findById(2L).orElseThrow().getStockQuantity()).isEqualTo(3);
        assertThat(orderRepository.count()).isEqualTo(2);
    }

    @Test
    void createOrderRollsBackStockAndOrderWhenStockInsufficient() throws Exception {
        String body = orderBody("在庫不足テスト", List.of(
                Map.of("productId", 1, "quantity", 2),
                Map.of("productId", 3, "quantity", 4)));

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("「USB-Cハブ (7in1)」の在庫が不足しています(在庫数: 3)。"));

        assertThat(productRepository.findById(1L).orElseThrow().getStockQuantity()).isEqualTo(24);
        assertThat(productRepository.findById(3L).orElseThrow().getStockQuantity()).isEqualTo(3);
        assertThat(orderRepository.count()).isEqualTo(1);
    }

    @Test
    void createOrderRejectsMissingCustomerNameWith400() throws Exception {
        String body = orderBody("  ", List.of(Map.of("productId", 1, "quantity", 1)));

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.customerName").exists());

        assertThat(productRepository.findById(1L).orElseThrow().getStockQuantity()).isEqualTo(24);
    }

    @Test
    void createOrderRejectsOrderWithoutPositiveQuantityWith400() throws Exception {
        String body = orderBody("得意先", List.of(Map.of("productId", 1, "quantity", 0)));

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("少なくとも1つの商品を数量1以上で選択してください。"));

        assertThat(orderRepository.count()).isEqualTo(1);
    }
}
