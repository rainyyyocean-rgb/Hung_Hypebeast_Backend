package org.example.hung_hypebeast_backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/sepay-webhook-mock")
    public ResponseEntity<?> mockSePayWebhook(@RequestBody Map<String, String> payload) {
        String token = payload.get("trackingToken");

        try {
            // Controller chỉ gọi Service, không biết logic bên trong làm gì
            paymentService.processSePayWebhook(token);
            return ResponseEntity.ok("Webhook processed successfully.");

        } catch (RuntimeException e) {
            // Nếu Service báo lỗi thì trả về 400
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
