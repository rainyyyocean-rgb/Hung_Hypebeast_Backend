package org.example.hung_hypebeast_backend.controller;

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
public class CartController {

    private final CartService cartService;

    @PostMapping("/validate")
    public ResponseEntity<List<CartValidationResponse>> validateCart(@RequestBody List<CartItemRequest> cartItems) {
        return ResponseEntity.ok(cartService.validateCart(cartItems));
    }
}
