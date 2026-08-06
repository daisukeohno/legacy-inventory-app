package com.example.inventory.service;

import com.example.inventory.config.InventoryProperties;
import com.example.inventory.domain.Product;
import com.example.inventory.dto.ProductRequest;
import com.example.inventory.repository.ProductRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final int lowStockThreshold;

    public ProductService(ProductRepository productRepository, InventoryProperties properties) {
        this.productRepository = productRepository;
        this.lowStockThreshold = properties.lowStockThresholdOrDefault();
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    /** 旧 ProductDao.search() と同一の絞り込み(キーワードは name/sku の部分一致・大文字小文字無視)。 */
    @Transactional(readOnly = true)
    public List<Product> search(String keyword, boolean lowStockOnly) {
        String normalized = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        return productRepository.search(normalized, lowStockOnly, lowStockThreshold);
    }

    @Transactional(readOnly = true)
    public Product findById(int id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public Product create(ProductRequest request) {
        Product product = new Product(request.sku().trim(), request.name().trim(),
                request.price(), request.stockQuantity());
        Product saved = productRepository.save(product);
        log.info("Created product id={} sku={}", saved.getId(), saved.getSku());
        return saved;
    }

    @Transactional
    public Product update(int id, ProductRequest request) {
        Product product = findById(id);
        product.setSku(request.sku().trim());
        product.setName(request.name().trim());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        log.info("Updated product id={} sku={}", id, product.getSku());
        return product;
    }
}
