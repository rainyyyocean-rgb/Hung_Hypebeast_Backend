package org.example.hung_hypebeast_backend.service;

import org.example.hung_hypebeast_backend.dto.response.OrderResponse;

public interface PaymentService {
    OrderResponse processSePayWebhook(String trackingToken);
}
