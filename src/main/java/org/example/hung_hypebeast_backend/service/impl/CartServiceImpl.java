package org.example.hung_hypebeast_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.dto.request.CartItemRequest;
import org.example.hung_hypebeast_backend.dto.response.CartValidationResponse;
import org.example.hung_hypebeast_backend.entity.ProductSku;
import org.example.hung_hypebeast_backend.repository.ProductSkuRepository;
import org.example.hung_hypebeast_backend.service.CartService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ProductSkuRepository productSkuRepository;

    @Override
    public List<CartValidationResponse> validateCart(List<CartItemRequest> items) {
        // 1. Lấy danh sách ID từ request
        List<Long> skuIds = items.stream().map(CartItemRequest::getSkuId).toList();

        // 2. Query DB lấy thông tin mới nhất của các SKU này
        List<ProductSku> skusInDb = productSkuRepository.findAllById(skuIds);

        // Chuyển List thành Map<ID, SKU> để dễ tra cứu
        Map<Long, ProductSku> skuMap = skusInDb.stream()
                .collect(Collectors.toMap(ProductSku::getId, Function.identity()));

        List<CartValidationResponse> result = new ArrayList<>();

        // 3. Duyệt từng item khách gửi lên để check
        for (CartItemRequest itemReq : items) {
            ProductSku sku = skuMap.get(itemReq.getSkuId());

            // Case 1: Sản phẩm đã bị xóa hoặc ngừng bán
            if (sku == null) {
                result.add(CartValidationResponse.builder()
                        .skuId(itemReq.getSkuId())
                        .isValid(false)
                        .message("Sản phẩm không tồn tại hoặc đã ngừng bán")
                        .currentStock(0)
                        .build());
                continue;
            }

            // Case 2: Check số lượng
            boolean isEnough = sku.getQuantity() >= itemReq.getQuantity();
            String msg = isEnough ? "Còn hàng" : "Kho chỉ còn " + sku.getQuantity() + " sản phẩm";

            result.add(CartValidationResponse.builder()
                    .skuId(sku.getId())
                    .productName(sku.getProduct().getName())
                    .variantName(sku.getSize() + " / " + sku.getColor())
                    .isValid(isEnough)
                    .message(msg)
                    .currentStock(sku.getQuantity()) // Trả về số thực để FE tự sửa lại số lượng
                    .currentPrice(sku.getPrice() != null ? sku.getPrice() : sku.getProduct().getBasePrice())
                    .build());
        }

        return result;
    }
}