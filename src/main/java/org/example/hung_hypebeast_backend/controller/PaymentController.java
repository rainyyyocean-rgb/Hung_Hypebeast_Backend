package org.example.hung_hypebeast_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.dto.request.SePayWebhookRequest;
import org.example.hung_hypebeast_backend.dto.response.OrderResponse;
import org.example.hung_hypebeast_backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "API xử lý thanh toán")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/sepay-webhook-mock")
    @Operation(
            summary = "Webhook xử lý thanh toán SePay (Mock)",
            description = "Endpoint giả lập webhook từ SePay để xác nhận thanh toán. Khi thanh toán thành công, đơn hàng sẽ được cập nhật trạng thái từ PENDING sang CONFIMRED"
    )
    public ResponseEntity<OrderResponse> mockSePayWebhook(
            @Valid @RequestBody SePayWebhookRequest request) {

        System.out.println("📥 [PaymentController] Nhận request webhook với token: " + request.getTrackingToken());

        // Controller chỉ nhận request và gọi Service
        // Validation được xử lý bởi @Valid annotation
        // Business logic và exception handling ở Service layer
        OrderResponse response = paymentService.processSePayWebhook(request.getTrackingToken());

        System.out.println("✅ [PaymentController] Webhook processed successfully");
        return ResponseEntity.ok(response);
    }
}
