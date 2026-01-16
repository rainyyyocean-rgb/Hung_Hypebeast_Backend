package org.example.hung_hypebeast_backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.dto.request.OrderRequest;
import org.example.hung_hypebeast_backend.dto.response.OrderResponse;
import org.example.hung_hypebeast_backend.enums.OrderStatus;
import org.example.hung_hypebeast_backend.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    //api tạo đơn hàng (khách hàng đặt)
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    // API: Xem danh sách đơn hàng (Dành cho Admin)
    @GetMapping("/admin")
    public ResponseEntity<Page<OrderResponse>> getOrdersForAdmin(
            @RequestParam(required = false) OrderStatus status, // Lọc theo trạng thái
            @RequestParam(required = false) String phone,       // Tìm theo sđt
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok(orderService.getOrdersForAdmin(status, phone, pageable));
    }
}
