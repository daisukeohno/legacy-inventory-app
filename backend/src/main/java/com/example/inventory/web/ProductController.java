package com.example.inventory.web;

import com.example.inventory.dto.ProductDto;
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
    public List<ProductDto> list(@RequestParam(required = false) String keyword,
                                 @RequestParam(defaultValue = "false") boolean lowStockOnly) {
        return productService.search(keyword, lowStockOnly);
    }

    @GetMapping("/{id}")
    public ProductDto get(@PathVariable Integer id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto create(@Valid @RequestBody ProductDto product) {
        return productService.create(product);
    }

    @PutMapping("/{id}")
    public ProductDto update(@PathVariable Integer id, @Valid @RequestBody ProductDto product) {
        return productService.update(id, product);
    }
}
