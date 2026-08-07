package com.example.inventory.web;

import com.example.inventory.domain.Product;
import com.example.inventory.service.ProductService;
import com.example.inventory.web.dto.ProductRequest;
import com.example.inventory.web.dto.ProductResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> list(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false, defaultValue = "false") boolean lowStock) {
        int threshold = productService.getLowStockThreshold();
        return productService.search(keyword, lowStock).stream()
                .map(p -> ProductResponse.from(p, threshold))
                .toList();
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return ProductResponse.from(productService.findById(id), productService.getLowStockThreshold());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        Product product = productService.create(request.sku(), request.name(), request.price(),
                request.stockQuantity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponse.from(product, productService.getLowStockThreshold()));
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        Product product = productService.update(id, request.sku(), request.name(), request.price(),
                request.stockQuantity());
        return ProductResponse.from(product, productService.getLowStockThreshold());
    }
}
