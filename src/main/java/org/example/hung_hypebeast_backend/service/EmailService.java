package org.example.hung_hypebeast_backend.service;

import org.example.hung_hypebeast_backend.entity.Order;

public interface EmailService {
    void sendOrderConfirmationEmail(Order order);
}

