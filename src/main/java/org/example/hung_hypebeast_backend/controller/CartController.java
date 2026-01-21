package org.example.hung_hypebeast_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.dto.request.CartItemRequest;
import org.example.hung_hypebeast_backend.dto.response.CartValidationResponse;
import org.example.hung_hypebeast_backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "API check tồn kho khi thêm vào giỏ")
public class CartController {

    private final CartService cartService;

    @PostMapping("/validate")
    @Operation(
            summary = "Kiểm tra tồn kho giỏ hàng",
            description = "Xác thực tính khả dụng của các sản phẩm trong giỏ hàng, kiểm tra số lượng tồn kho và trả về trạng thái từng item"
    )
    public ResponseEntity<List<CartValidationResponse>> validateCart(@RequestBody List<CartItemRequest> cartItems) {
        return ResponseEntity.ok(cartService.validateCart(cartItems));
    }
}
