package com.example.inventory.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventory.config.AppProperties;
import com.example.inventory.config.WebConfig;
import com.example.inventory.domain.Product;
import com.example.inventory.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@Import(WebConfig.class)
@EnableConfigurationProperties(AppProperties.class)
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProductService productService;

    @Test
    void listReturnsProductsAsJson() throws Exception {
        given(productService.search(any(), anyBoolean()))
                .willReturn(List.of(new Product("SKU-1002", "ワイヤレスマウス", new BigDecimal("2800"), 6)));
        given(productService.isLowStock(any())).willReturn(true);

        mockMvc.perform(get("/api/products").param("keyword", "マウス").param("lowStockOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU-1002"))
                .andExpect(jsonPath("$[0].price").value(2800))
                .andExpect(jsonPath("$[0].lowStock").value(true));
    }

    @Test
    void createReturns201() throws Exception {
        given(productService.create(anyString(), anyString(), any(), anyInt()))
                .willReturn(new Product("SKU-2001", "新商品", new BigDecimal("1000"), 5));

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-2001","name":"新商品","price":1000,"stockQuantity":5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("新商品"))
                .andExpect(jsonPath("$.price").value(1000));
    }

    @Test
    void negativePriceReturns400() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-2001","name":"新商品","price":-1,"stockQuantity":5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value(org.hamcrest.Matchers.containsString("price")));
    }

    @Test
    void corsPreflightIsAllowedForConfiguredOriginAndRejectedOtherwise() throws Exception {
        mockMvc.perform(options("/api/products")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", "http://localhost:5173"));

        mockMvc.perform(options("/api/products")
                        .header("Origin", "http://evil.example.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
