package com.example.inventory.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventory.domain.Product;
import com.example.inventory.service.ProductService;
import com.example.inventory.service.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @BeforeEach
    void setUp() {
        given(productService.getLowStockThreshold()).willReturn(10);
    }

    @Test
    @DisplayName("GET /api/products は低在庫フラグ付きの商品一覧を返す")
    void listProducts() throws Exception {
        given(productService.search(eq("マウス"), anyBoolean())).willReturn(
                List.of(new Product("SKU-1002", "ワイヤレスマウス", new BigDecimal("2800.00"), 6)));

        mockMvc.perform(get("/api/products").param("keyword", "マウス").param("lowStockOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU-1002"))
                .andExpect(jsonPath("$[0].price").value(2800.00))
                .andExpect(jsonPath("$[0].lowStock").value(true));
    }

    @Test
    @DisplayName("GET /api/products/{id} は存在しない場合404")
    void getProductNotFound() throws Exception {
        willThrow(new ResourceNotFoundException("商品が見つかりません(id: 42)。"))
                .given(productService).findById(42L);

        mockMvc.perform(get("/api/products/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/products は201を返す")
    void createProduct() throws Exception {
        given(productService.create(any(), any(), any(), anyInt()))
                .willReturn(new Product("SKU-9001", "新商品", new BigDecimal("1500.00"), 20));

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-9001","name":"新商品","price":1500.00,"stockQuantity":20}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("新商品"))
                .andExpect(jsonPath("$.lowStock").value(false));
    }

    @Test
    @DisplayName("POST /api/products はバリデーション違反で400 + 旧 ProductForm と同じメッセージ")
    void createProductValidationError() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"","name":"","price":-1,"stockQuantity":-5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[*].message",
                        org.hamcrest.Matchers.hasItems("SKUを入力してください。", "商品名を入力してください。",
                                "単価は0以上で入力してください。", "在庫数は0以上で入力してください。")));
    }

    @Test
    @DisplayName("PUT /api/products/{id} は更新結果を返す")
    void updateProduct() throws Exception {
        given(productService.update(anyLong(), any(), any(), any(), anyInt()))
                .willReturn(new Product("SKU-1002", "ワイヤレスマウス(改)", new BigDecimal("2900.00"), 6));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/products/2").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-1002","name":"ワイヤレスマウス(改)","price":2900.00,"stockQuantity":6}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ワイヤレスマウス(改)"))
                .andExpect(jsonPath("$.lowStock").value(true));
    }
}
