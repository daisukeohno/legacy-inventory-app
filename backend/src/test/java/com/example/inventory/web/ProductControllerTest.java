package com.example.inventory.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventory.domain.Product;
import com.example.inventory.service.NotFoundException;
import com.example.inventory.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void listReturnsProductsWithLowStockFlag() throws Exception {
        Product product = new Product("SKU-1003", "USB-Cハブ (7in1)", new BigDecimal("4500"), 3);
        given(productService.search(eq("hub"), eq(true))).willReturn(List.of(product));
        given(productService.isLowStock(any())).willReturn(true);

        mockMvc.perform(get("/api/products").param("keyword", "hub").param("lowStockOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU-1003"))
                .andExpect(jsonPath("$[0].price").value(4500))
                .andExpect(jsonPath("$[0].lowStock").value(true));
    }

    @Test
    void getUnknownProductReturns404() throws Exception {
        given(productService.findById(anyLong())).willThrow(new NotFoundException("商品が見つかりません (id=999)"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("商品が見つかりません (id=999)"));
    }

    @Test
    void createReturns201() throws Exception {
        given(productService.create(anyString(), anyString(), any(), org.mockito.ArgumentMatchers.anyInt()))
                .willReturn(new Product("SKU-2001", "新商品", new BigDecimal("1500"), 20));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU-2001\",\"name\":\"新商品\",\"price\":1500,\"stockQuantity\":20}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("新商品"));
    }

    @Test
    void negativePriceIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU-2001\",\"name\":\"新商品\",\"price\":-1,\"stockQuantity\":20}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void blankSkuIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"\",\"name\":\"新商品\",\"price\":100,\"stockQuantity\":1}"))
                .andExpect(status().isBadRequest());
    }
}
