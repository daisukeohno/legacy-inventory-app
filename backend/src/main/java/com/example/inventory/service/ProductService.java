package com.example.inventory.service;

import com.example.inventory.config.InventoryProperties;
import com.example.inventory.domain.Product;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryProperties inventoryProperties;

    public ProductService(ProductRepository productRepository, InventoryProperties inventoryProperties) {
        this.productRepository = productRepository;
        this.inventoryProperties = inventoryProperties;
    }

    @Transactional(readOnly = true)
    public List<Product> search(String keyword, boolean lowStockOnly) {
        String normalized = keyword == null ? "" : keyword.trim();
        return productRepository.search(normalized, lowStockOnly, inventoryProperties.getLowStockThreshold());
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @Transactional
    public Product create(String sku, String name, java.math.BigDecimal price, int stockQuantity) {
        return productRepository.save(new Product(sku, name, price, stockQuantity));
    }

    @Transactional
    public Product update(Long id, String sku, String name, java.math.BigDecimal price, int stockQuantity) {
        Product product = findById(id);
        product.setSku(sku);
        product.setName(name);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        return productRepository.save(product);
    }

    public boolean isLowStock(Product product) {
        return product.isLowStock(inventoryProperties.getLowStockThreshold());
    }

    public int getLowStockThreshold() {
        return inventoryProperties.getLowStockThreshold();
    }
}
