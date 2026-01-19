package org.example.hung_hypebeast_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.dto.request.CartItemRequest;
import org.example.hung_hypebeast_backend.dto.response.CartValidationResponse;
import org.example.hung_hypebeast_backend.entity.ProductSku;
import org.example.hung_hypebeast_backend.mapper.CartMapper;
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
    private final CartMapper cartMapper;

    @Override
    public List<CartValidationResponse> validateCart(List<CartItemRequest> items) {
        // 1. Lấy danh sách ID từ request
        List<Long> skuIds = items.stream()
                .map(CartItemRequest::getSkuId)
                .toList();

        // 2. Query DB lấy thông tin mới nhất của các SKU này
        List<ProductSku> skusInDb = productSkuRepository.findAllById(skuIds);

        // 3. Chuyển List thành Map<ID, SKU> để dễ tra cứu
        Map<Long, ProductSku> skuMap = skusInDb.stream()
                .collect(Collectors.toMap(ProductSku::getId, Function.identity()));

        List<CartValidationResponse> result = new ArrayList<>();

        // 4. Duyệt từng item khách gửi lên để validate
        for (CartItemRequest itemReq : items) {
            ProductSku sku = skuMap.get(itemReq.getSkuId());

            // Case 1: Sản phẩm đã bị xóa hoặc ngừng bán
            if (sku == null) {
                result.add(cartMapper.toNotFoundResponse(itemReq.getSkuId()));
                continue;
            }

            // Case 2: Sản phẩm tồn tại - validate số lượng
            result.add(cartMapper.toValidationResponse(sku, itemReq.getQuantity()));
        }

        return result;
    }
}