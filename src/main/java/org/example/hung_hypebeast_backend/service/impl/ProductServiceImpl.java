package org.example.hung_hypebeast_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.dto.response.CategoryResponse;
import org.example.hung_hypebeast_backend.dto.response.ProductResponse;
import org.example.hung_hypebeast_backend.dto.response.ProductSkuDto;
import org.example.hung_hypebeast_backend.entity.Product;
import org.example.hung_hypebeast_backend.exception.ResourceNotFoundException;
import org.example.hung_hypebeast_backend.repository.ProductRepository;
import org.example.hung_hypebeast_backend.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service // Annotation này phải đặt ở class Impl, không đặt ở Interface
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;


    @Override
    public Page<ProductResponse> getProducts(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        // 1. Gọi Repo lấy Page<Entity>
        Page<Product> productPage = productRepository.searchProducts(categoryId, minPrice, maxPrice, pageable);

        // 2. Map từng Entity sang DTO (Giữ nguyên cấu trúc Page)
        return productPage.map(this::mapToDto);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToDto(product);
    }

    // Hàm helper để convert thủ công (Hoặc dùng MapStruct nếu muốn xịn hơn)
    private ProductResponse mapToDto(Product product) {
        List<ProductSkuDto> skuDtos = product.getSkus().stream()
                .map(sku -> ProductSkuDto.builder()
                        .id(sku.getId())
                        .skuCode(sku.getSkuCode())
                        .size(sku.getSize())
                        .color(sku.getColor())
                        .quantity(sku.getQuantity())
                        // THÊM LOGIC: Nếu SKU không có giá riêng thì lấy giá gốc của sản phẩm
                        .price(sku.getPrice() != null ? sku.getPrice() : product.getBasePrice())
                        .build())
                .collect(Collectors.toList());

        // 2. Map Category (MỚI)
        CategoryResponse categoryDto = null;
        if (product.getCategory() != null) {
            categoryDto = CategoryResponse.builder()
                    .id(product.getCategory().getId())
                    .name(product.getCategory().getName())
                    .build();
        }

        // 3. Build ProductResponse
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getBasePrice())
                .category(categoryDto) // <--- Set object DTO vào đây
                .variants(skuDtos)
                .build();
    }
}

