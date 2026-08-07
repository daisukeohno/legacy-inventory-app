package com.example.inventory.service;

import com.example.inventory.config.InventoryProperties;
import com.example.inventory.domain.Product;
import com.example.inventory.repository.ProductRepository;
import java.math.BigDecimal;
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
        return productRepository.search(normalized, lowStockOnly, lowStockThreshold());
    }

    @Transactional(readOnly = true)
    public Product findById(long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("商品が見つかりません (id=" + id + ")"));
    }

    @Transactional
    public Product create(String sku, String name, BigDecimal price, int stockQuantity) {
        return productRepository.save(new Product(sku, name, price, stockQuantity));
    }

    @Transactional
    public Product update(long id, String sku, String name, BigDecimal price, int stockQuantity) {
        Product product = findById(id);
        product.setSku(sku);
        product.setName(name);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        return productRepository.save(product);
    }

    /**
     * 低在庫判定。将来の商品ごとしきい値対応のため、判定はこのメソッドに集約する。
     */
    public boolean isLowStock(Product product) {
        return product.getStockQuantity() < lowStockThreshold();
    }

    public int lowStockThreshold() {
        return inventoryProperties.lowStockThreshold();
    }
}
