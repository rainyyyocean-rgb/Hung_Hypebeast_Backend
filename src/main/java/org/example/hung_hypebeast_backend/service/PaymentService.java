package org.example.hung_hypebeast_backend.service;

public interface PaymentService {
    void processSePayWebhook(String trackingToken);
}
