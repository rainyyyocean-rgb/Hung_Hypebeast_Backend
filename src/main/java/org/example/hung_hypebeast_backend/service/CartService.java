package org.example.hung_hypebeast_backend.service;

import org.example.hung_hypebeast_backend.dto.request.CartItemRequest;
import org.example.hung_hypebeast_backend.dto.response.CartValidationResponse;

import java.util.List;

public interface CartService {
    List<CartValidationResponse> validateCart(List<CartItemRequest> request);
}
