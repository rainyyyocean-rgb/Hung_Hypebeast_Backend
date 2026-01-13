package org.example.hung_hypebeast_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductSkuDto {
    private Long id;
    private String skuCode;
    private String size;
    private String color;
    private Integer quantity; // Cho khách biết còn bao nhiêu hàng
    private BigDecimal price;
}