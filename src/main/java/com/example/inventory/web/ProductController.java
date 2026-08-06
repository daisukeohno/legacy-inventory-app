package com.example.inventory.web;

import com.example.inventory.dto.ProductRequest;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.service.ProductService;
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
        int threshold = productService.getLowStockThreshold();
        return productService.search(keyword, lowStockOnly).stream()
                .map(product -> ProductResponse.from(product, threshold))
                .toList();
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable int id) {
        return ProductResponse.from(productService.findById(id), productService.getLowStockThreshold());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return ProductResponse.from(productService.create(request), productService.getLowStockThreshold());
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable int id, @Valid @RequestBody ProductRequest request) {
        return ProductResponse.from(productService.update(id, request), productService.getLowStockThreshold());
    }
}
