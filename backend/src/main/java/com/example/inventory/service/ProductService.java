package com.example.inventory.service;

import com.example.inventory.dto.ProductDto;
import com.example.inventory.entity.Product;
import com.example.inventory.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * キーワード(商品名/SKUの大文字小文字を無視した部分一致)と低在庫フラグで絞り込む。
     */
    public List<ProductDto> search(String keyword, boolean lowStockOnly) {
        String normalized = (keyword == null || keyword.isBlank()) ? null : escapeLikePattern(keyword.trim());
        return productRepository.search(normalized, lowStockOnly, Product.LOW_STOCK_THRESHOLD)
                .stream()
                .map(ProductDto::from)
                .toList();
    }

    /** LIKE のワイルドカードをリテラルとして扱う（旧実装の String.contains と同じ一致条件）。 */
    private static String escapeLikePattern(String keyword) {
        return keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    public ProductDto findById(Integer id) {
        return productRepository.findById(id)
                .map(ProductDto::from)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public ProductDto create(ProductDto dto) {
        Product product = new Product(dto.sku(), dto.name(), dto.price(), dto.stockQuantity());
        return ProductDto.from(productRepository.save(product));
    }

    @Transactional
    public ProductDto update(Integer id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.setSku(dto.sku());
        product.setName(dto.name());
        product.setPrice(dto.price());
        product.setStockQuantity(dto.stockQuantity());
        return ProductDto.from(productRepository.save(product));
    }
}
