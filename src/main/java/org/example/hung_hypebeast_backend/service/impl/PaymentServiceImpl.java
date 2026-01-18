package org.example.hung_hypebeast_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.entity.Order;
import org.example.hung_hypebeast_backend.enums.OrderStatus;
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

    @Override
    @Transactional // Logic DB phải nằm ở Service để đảm bảo Transaction
    public void processSePayWebhook(String token) {
        // 1. Tìm đơn hàng
        Order order = orderRepository.findByTrackingToken(token)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với token: " + token));

        // 2. Validate trạng thái
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ thanh toán (PENDING).");
        }

        // 3. Cập nhật trạng thái & Xóa hạn sử dụng
        order.setStatus(OrderStatus.CONFIRMED); // Đã thanh toán = Đã xác nhận
        order.setExpiredAt(null); // Xóa expired để Cronjob không quét

        orderRepository.save(order);

        System.out.println("Payment Success for Order ID: " + order.getId());

        // 4. Gửi email xác nhận thanh toán thành công
        // Trigger lazy loading của items trước khi gửi email
        order.getItems().size(); // Force load items from database
        emailService.sendOrderConfirmationEmail(order);
    }
}
