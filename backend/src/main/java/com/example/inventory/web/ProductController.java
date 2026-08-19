package com.example.inventory.web;

import com.example.inventory.domain.Product;
import com.example.inventory.service.ProductService;
import com.example.inventory.web.dto.ProductDto;
import com.example.inventory.web.dto.ProductRequest;
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
    public List<ProductDto> list(@RequestParam(required = false) String keyword,
                                 @RequestParam(defaultValue = "false") boolean lowStockOnly) {
        int threshold = productService.getLowStockThreshold();
        return productService.search(keyword, lowStockOnly).stream()
                .map(product -> ProductDto.from(product, threshold))
                .toList();
    }

    @GetMapping("/{id}")
    public ProductDto get(@PathVariable Long id) {
        return ProductDto.from(productService.findById(id), productService.getLowStockThreshold());
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductRequest request) {
        Product created = productService.create(request.sku(), request.name(), request.price(),
                request.stockQuantity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductDto.from(created, productService.getLowStockThreshold()));
    }

    @PutMapping("/{id}")
    public ProductDto update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        Product updated = productService.update(id, request.sku(), request.name(), request.price(),
                request.stockQuantity());
        return ProductDto.from(updated, productService.getLowStockThreshold());
    }
}
