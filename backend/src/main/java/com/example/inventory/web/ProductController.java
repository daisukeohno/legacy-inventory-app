package com.example.inventory.web;

import com.example.inventory.domain.Product;
import com.example.inventory.service.ProductService;
import com.example.inventory.web.dto.ProductRequest;
import com.example.inventory.web.dto.ProductResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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
                                      @RequestParam(defaultValue = "false") boolean lowStockOnly) {
        return productService.search(keyword, lowStockOnly).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable long id) {
        return toResponse(productService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return toResponse(productService.create(request.sku(), request.name(),
                request.price(), request.stockQuantity()));
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable long id, @Valid @RequestBody ProductRequest request) {
        return toResponse(productService.update(id, request.sku(), request.name(),
                request.price(), request.stockQuantity()));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.of(product, productService.isLowStock(product));
    }
}
