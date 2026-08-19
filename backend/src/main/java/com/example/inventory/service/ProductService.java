package com.example.inventory.service;

import com.example.inventory.config.InventoryProperties;
import com.example.inventory.domain.Order;
import com.example.inventory.domain.Product;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.web.dto.ProductDto;
import com.example.inventory.web.dto.ProductRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final InventoryProperties properties;

    public ProductService(ProductRepository productRepository, InventoryProperties properties) {
        this.productRepository = productRepository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> search(String keyword, boolean lowStockOnly) {
        String normalized = (keyword == null) ? "" : keyword.trim();
        return productRepository.search(normalized, lowStockOnly, properties.getLowStockThreshold())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        return toDto(getProduct(id));
    }

    @Transactional
    public ProductDto create(ProductRequest request) {
        Product product = new Product(
                request.sku().trim(),
                request.name().trim(),
                normalizePrice(request.price()),
                request.stockQuantity()
        );
        Product saved = productRepository.save(product);
        log.info("Created product id={} sku={}", saved.getId(), saved.getSku());
        return toDto(saved);
    }

    @Transactional
    public ProductDto update(Long id, ProductRequest request) {
        Product product = getProduct(id);
        product.setSku(request.sku().trim());
        product.setName(request.name().trim());
        product.setPrice(normalizePrice(request.price()));
        product.setStockQuantity(request.stockQuantity());
        log.info("Updated product id={} sku={}", product.getId(), product.getSku());
        return toDto(product);
    }

    /** 金額カラムは NUMERIC(12,0)（円単位）なので、保存前に合計計算と同じスケール方針へ丸める。 */
    private BigDecimal normalizePrice(BigDecimal price) {
        return price.setScale(Order.AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    private ProductDto toDto(Product product) {
        return ProductDto.of(product, properties.getLowStockThreshold());
    }
}
