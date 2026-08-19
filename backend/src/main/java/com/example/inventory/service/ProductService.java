package com.example.inventory.service;

import com.example.inventory.domain.Product;
import com.example.inventory.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 旧 ProductListAction / ProductEditAction / ProductSaveAction の業務ロジックを集約したサービス。
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final int lowStockThreshold;

    public ProductService(ProductRepository productRepository,
                          @Value("${inventory.low-stock-threshold:10}") int lowStockThreshold) {
        this.productRepository = productRepository;
        this.lowStockThreshold = lowStockThreshold;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public List<Product> search(String keyword, boolean lowStockOnly) {
        String normalized = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        if (normalized == null) {
            return lowStockOnly
                    ? productRepository.findByStockQuantityLessThanOrderByIdAsc(lowStockThreshold)
                    : productRepository.findAllByOrderByIdAsc();
        }
        return lowStockOnly
                ? productRepository.searchLowStock(normalized, lowStockThreshold)
                : productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCaseOrderByIdAsc(
                        normalized, normalized);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品が見つかりません(id: " + id + ")。"));
    }

    @Transactional
    public Product create(String sku, String name, BigDecimal price, int stockQuantity) {
        return productRepository.save(new Product(sku, name, price, stockQuantity));
    }

    @Transactional
    public Product update(Long id, String sku, String name, BigDecimal price, int stockQuantity) {
        Product product = findById(id);
        product.setSku(sku);
        product.setName(name);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        return productRepository.save(product);
    }
}
