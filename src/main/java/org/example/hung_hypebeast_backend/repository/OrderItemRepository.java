package org.example.hung_hypebeast_backend.repository;

import org.example.hung_hypebeast_backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
