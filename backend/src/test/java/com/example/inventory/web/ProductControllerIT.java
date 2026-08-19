package com.example.inventory.web;

import com.example.inventory.AbstractPostgresIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ProductControllerIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listReturnsSeededProductsWithLowStockFlag() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(6)))
                .andExpect(jsonPath("$[0].sku").value("SKU-1001"))
                .andExpect(jsonPath("$[0].price").value(128000))
                .andExpect(jsonPath("$[0].lowStock").value(false))
                .andExpect(jsonPath("$[1].lowStock").value(true));
    }

    @Test
    void listAppliesKeywordAndLowStockFilter() throws Exception {
        mockMvc.perform(get("/api/products").param("keyword", "ハブ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].sku").value("SKU-1003"));

        mockMvc.perform(get("/api/products").param("lowStockOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(3)));
    }

    @Test
    void getByIdReturns404ForUnknownProduct() throws Exception {
        mockMvc.perform(get("/api/products/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByIdReturns400ForMalformedId() throws Exception {
        mockMvc.perform(get("/api/products/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void unsupportedMethodReturns405() throws Exception {
        mockMvc.perform(delete("/api/products"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void createRoundsDecimalPriceToWholeYen() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "sku", "SKU-2002", "name", "小数価格商品", "price", "1200.5", "stockQuantity", 20));

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(1201));
    }

    @Test
    void createPersistsProduct() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "sku", "SKU-2001", "name", "ドッキングステーション", "price", 23800, "stockQuantity", 4));

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.lowStock").value(true));

        mockMvc.perform(get("/api/products").param("keyword", "SKU-2001"))
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void updateModifiesExistingProduct() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "sku", "SKU-1001", "name", "ノートPC 14インチ (2026)", "price", 132000, "stockQuantity", 9));

        mockMvc.perform(put("/api/products/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ノートPC 14インチ (2026)"))
                .andExpect(jsonPath("$.price").value(132000))
                .andExpect(jsonPath("$.lowStock").value(true));
    }

    @Test
    void createRejectsInvalidPayloadWith400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "sku", "", "name", "", "price", -1, "stockQuantity", -5));

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.sku").exists())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists())
                .andExpect(jsonPath("$.fieldErrors.stockQuantity").exists());
    }

    @Test
    void createRejectsNonNumericPriceWith400() throws Exception {
        String body = """
                {"sku":"SKU-3001","name":"不正価格","price":"abc","stockQuantity":1}
                """;

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }
}
