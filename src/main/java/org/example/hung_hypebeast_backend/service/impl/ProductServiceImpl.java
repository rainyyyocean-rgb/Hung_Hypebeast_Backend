package org.example.hung_hypebeast_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.dto.response.ProductResponse;
import org.example.hung_hypebeast_backend.entity.Product;
import org.example.hung_hypebeast_backend.exception.ResourceNotFoundException;
import org.example.hung_hypebeast_backend.mapper.ProductMapper;
import org.example.hung_hypebeast_backend.repository.ProductRepository;
import org.example.hung_hypebeast_backend.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;


    @Override
    public Page<ProductResponse> getProducts(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        // 1. Gọi Repository lấy Page<Entity>
        Page<Product> productPage = productRepository.searchProducts(categoryId, minPrice, maxPrice, pageable);

        // 2. Sử dụng mapper để chuyển đổi từng Entity sang DTO
        return productPage.map(productMapper::toProductResponse);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        // 1. Tìm product trong database
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // 2. Sử dụng mapper để convert sang DTO
        return productMapper.toProductResponse(product);
    }
}

