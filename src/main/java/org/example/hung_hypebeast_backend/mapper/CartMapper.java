package org.example.hung_hypebeast_backend.mapper;

import org.example.hung_hypebeast_backend.dto.response.CartValidationResponse;
import org.example.hung_hypebeast_backend.entity.ProductSku;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper để chuyển đổi dữ liệu liên quan đến Cart
 */
@Component
public class CartMapper {

    /**
     * Tạo response khi sản phẩm không tồn tại
     *
     * @param skuId ID của SKU
     * @return CartValidationResponse với thông báo lỗi
     */
    public CartValidationResponse toNotFoundResponse(Long skuId) {
        return CartValidationResponse.builder()
                .skuId(skuId)
                .isValid(false)
                .message("Sản phẩm không tồn tại hoặc đã ngừng bán")
                .currentStock(0)
                .build();
    }

    /**
     * Tạo response validation cho SKU với số lượng yêu cầu
     *
     * @param sku ProductSku entity
     * @param requestedQuantity Số lượng khách yêu cầu
     * @return CartValidationResponse với thông tin chi tiết
     */
    public CartValidationResponse toValidationResponse(ProductSku sku, Integer requestedQuantity) {
        boolean isEnough = sku.getQuantity() >= requestedQuantity;
        String message = isEnough
                ? "Còn hàng"
                : "Kho chỉ còn " + sku.getQuantity() + " sản phẩm";

        // Lấy giá: ưu tiên giá SKU, nếu không có thì lấy giá base của product
        BigDecimal currentPrice = sku.getPrice() != null
                ? sku.getPrice()
                : sku.getProduct().getBasePrice();

        return CartValidationResponse.builder()
                .skuId(sku.getId())
                .productName(sku.getProduct().getName())
                .variantName(sku.getSize() + " / " + sku.getColor())
                .isValid(isEnough)
                .message(message)
                .currentStock(sku.getQuantity())
                .currentPrice(currentPrice)
                .build();
    }
}

