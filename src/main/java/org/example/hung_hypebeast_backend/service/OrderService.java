package org.example.hung_hypebeast_backend.service;

import org.example.hung_hypebeast_backend.dto.request.OrderRequest;
import org.example.hung_hypebeast_backend.dto.response.OrderResponse;
import org.example.hung_hypebeast_backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
    void cancelUnpaidOrders();
    Page<OrderResponse> getOrdersForAdmin(OrderStatus status, String phone, Pageable pageable);
}
