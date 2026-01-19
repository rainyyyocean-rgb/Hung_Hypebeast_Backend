package org.example.hung_hypebeast_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.dto.response.OrderResponse;
import org.example.hung_hypebeast_backend.entity.Order;
import org.example.hung_hypebeast_backend.enums.OrderStatus;
import org.example.hung_hypebeast_backend.exception.InvalidOrderStatusException;
import org.example.hung_hypebeast_backend.exception.OrderNotFoundException;
import org.example.hung_hypebeast_backend.mapper.OrderMapper;
import org.example.hung_hypebeast_backend.repository.OrderRepository;
import org.example.hung_hypebeast_backend.service.EmailService;
import org.example.hung_hypebeast_backend.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse processSePayWebhook(String token) {
        System.out.println("🔍 [PaymentService] Nhận webhook với token: " + token);

        // 1. Tìm đơn hàng - Tách riêng để tránh vấn đề với lambda và AOP proxy
        Order order = orderRepository.findByTrackingToken(token).orElse(null);

        if (order == null) {
            System.err.println("❌ [PaymentService] Không tìm thấy order với token: " + token);
            throw new OrderNotFoundException("mã tracking", token);
        }

        System.out.println("✅ [PaymentService] Tìm thấy order ID: " + order.getId() + ", Status: " + order.getStatus());

        // 2. Validate trạng thái
        if (order.getStatus() != OrderStatus.PENDING) {
            System.err.println("❌ [PaymentService] Order status không phải PENDING: " + order.getStatus());
            throw new InvalidOrderStatusException("Đơn hàng không ở trạng thái chờ thanh toán (PENDING).");
        }

        // 3. Cập nhật trạng thái & Xóa hạn sử dụng
        order.setStatus(OrderStatus.CONFIRMED); // Đã thanh toán = Đã xác nhận
        order.setExpiredAt(null); // Xóa expired để Cronjob không quét

        orderRepository.save(order);

        System.out.println("✅ Payment Success for Order ID: " + order.getId());

        // 4. Gửi email xác nhận thanh toán thành công
        try {
            // Trigger lazy loading của items trước khi gửi email
            order.getItems().size(); // Force load items from database
            emailService.sendOrderConfirmationEmail(order);
            System.out.println("✅ Email xác nhận đã được gửi đến: " + order.getCustomerEmail());
        } catch (Exception e) {
            // Log lỗi nhưng KHÔNG throw exception để tránh rollback transaction
            System.err.println("⚠️ Không thể gửi email cho đơn hàng #" + order.getId() + ": " + e.getMessage());
            e.printStackTrace();
            // Order vẫn được cập nhật thành CONFIRMED dù email fail
        }

        // 5. Trả về response
        return orderMapper.toUpdateStatusResponse(order);
    }
}
