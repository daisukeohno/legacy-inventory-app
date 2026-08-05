package com.example.inventory.service;

import com.example.inventory.config.AppProperties;
import com.example.inventory.domain.Product;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final AppProperties properties;

    public ProductService(ProductRepository productRepository, AppProperties properties) {
        this.productRepository = productRepository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<Product> search(String keyword, boolean lowStockOnly) {
        String normalized = keyword == null ? "" : keyword.trim();
        int threshold = lowStockOnly ? properties.getLowStockThreshold() : Integer.MAX_VALUE;
        return productRepository.search(normalized, threshold);
    }

    @Transactional(readOnly = true)
    public Product get(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("商品が見つかりません: id=" + id));
    }

    /**
     * 低在庫判定。現状はグローバルしきい値のみ。
     * 将来 Product が固有しきい値を持つ場合はここで上書き判定する。
     */
    public boolean isLowStock(Product product) {
        return product.getStockQuantity() < properties.getLowStockThreshold();
    }

    public int lowStockThreshold() {
        return properties.getLowStockThreshold();
    }

    @Transactional
    public Product create(String sku, String name, java.math.BigDecimal price, int stockQuantity) {
        return productRepository.save(new Product(sku, name, price, stockQuantity));
    }

    @Transactional
    public Product update(Long id, String sku, String name, java.math.BigDecimal price, int stockQuantity) {
        Product product = get(id);
        product.setSku(sku);
        product.setName(name);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        return productRepository.save(product);
    }
}
