package org.example.hung_hypebeast_backend.mapper;

import org.example.hung_hypebeast_backend.dto.response.CategoryResponse;
import org.example.hung_hypebeast_backend.dto.response.ProductResponse;
import org.example.hung_hypebeast_backend.dto.response.ProductSkuDto;
import org.example.hung_hypebeast_backend.entity.Category;
import org.example.hung_hypebeast_backend.entity.Product;
import org.example.hung_hypebeast_backend.entity.ProductSku;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper để chuyển đổi giữa Product/Category entities và các DTOs tương ứng
 */
@Component
public class ProductMapper {

    /**
     * Chuyển đổi Product entity sang ProductResponse DTO
     *
     * @param product Product entity
     * @return ProductResponse
     */
    public ProductResponse toProductResponse(Product product) {
        // 1. Map các SKU (variants)
        List<ProductSkuDto> skuDtos = product.getSkus().stream()
                .map(this::toProductSkuDto)
                .collect(Collectors.toList());

        // 2. Map Category
        CategoryResponse categoryDto = null;
        if (product.getCategory() != null) {
            categoryDto = toCategoryResponse(product.getCategory());
        }

        // 3. Build ProductResponse
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getBasePrice())
                .category(categoryDto)
                .variants(skuDtos)
                .build();
    }

    /**
     * Chuyển đổi ProductSku entity sang ProductSkuDto
     *
     * @param sku ProductSku entity
     * @return ProductSkuDto
     */
    public ProductSkuDto toProductSkuDto(ProductSku sku) {
        // Nếu SKU không có giá riêng thì lấy giá gốc của sản phẩm
        BigDecimal price = sku.getPrice() != null
                ? sku.getPrice()
                : sku.getProduct().getBasePrice();

        return ProductSkuDto.builder()
                .id(sku.getId())
                .skuCode(sku.getSkuCode())
                .size(sku.getSize())
                .color(sku.getColor())
                .quantity(sku.getQuantity())
                .price(price)
                .build();
    }

    /**
     * Chuyển đổi Category entity sang CategoryResponse DTO
     *
     * @param category Category entity
     * @return CategoryResponse
     */
    public CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}

