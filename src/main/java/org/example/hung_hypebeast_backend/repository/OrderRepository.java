package org.example.hung_hypebeast_backend.repository;

import org.example.hung_hypebeast_backend.entity.Order;
import org.example.hung_hypebeast_backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByTrackingToken(String trackingToken);
    Optional<Order> findAllByStatusAndExpiredAtBefore(OrderStatus orderStatus, LocalDateTime time);
    // Hàm tìm đơn cho Admin (Có lọc & Phân trang)
    @Query("SELECT o FROM Order o WHERE " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:phone IS NULL OR o.customerPhone LIKE %:phone%) " +
            "ORDER BY o.createdAt DESC") // Đơn mới nhất lên đầu
    Page<Order> findOrdersForAdmin(
            @Param("status") OrderStatus status,
            @Param("phone") String phone,
            Pageable pageable
    );
}
