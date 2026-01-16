package org.example.hung_hypebeast_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartValidationResponse {
    private Long skuId;
    private String productName;
    private String variantName; // VD: Size L - Màu Đen
    private Boolean isValid;    // true = Đủ hàng, false = Thiếu
    private String message;     // VD: "Chỉ còn 2 sản phẩm"
    private Integer currentStock; // Tồn kho thực tế
    private BigDecimal currentPrice; // Giá cập nhật (phòng khi Admin đổi giá)
}
