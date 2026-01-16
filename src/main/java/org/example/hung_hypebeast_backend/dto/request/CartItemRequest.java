package org.example.hung_hypebeast_backend.dto.request;

import lombok.Data;

@Data
public class CartItemRequest {
    private Long skuId;
    private Integer quantity;
}
