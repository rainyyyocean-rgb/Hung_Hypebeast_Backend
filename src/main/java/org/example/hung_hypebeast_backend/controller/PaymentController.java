package org.example.hung_hypebeast_backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.entity.Order;
import org.example.hung_hypebeast_backend.enums.OrderStatus;
import org.example.hung_hypebeast_backend.repository.OrderRepository;
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

    private final OrderRepository orderRepository;

    // API này giả vờ là SePay gọi về Backend của bạn khi khách chuyển khoản xong
    @PostMapping("/sepay-webhook-mock")
    public ResponseEntity<?> mockSePayWebhook(@RequestBody Map<String, String> payload) {
        // Payload giả lập: { "trackingToken": "aaaa-bbbb-cccc" }
        String token = payload.get("trackingToken");

        Order order = orderRepository.findByTrackingToken(token)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!"PENDING".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("Đơn hàng không ở trạng thái chờ thanh toán");
        }

        // Cập nhật sang ĐÃ THANH TOÁN
        // Lúc này hàng đã được giữ vĩnh viễn (cho đến khi giao xong)
        order.setStatus(OrderStatus.valueOf("PAID"));
        orderRepository.save(order);

        return ResponseEntity.ok("Webhook nhận thành công. Đơn hàng đã chuyển sang PAID.");
    }
}
