package org.example.hung_hypebeast_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "API xử lí order")
public class OrderController {

    private final OrderService orderService;

    //api tạo đơn hàng (khách hàng đặt)
    @PostMapping
    @Operation(
            summary = "Tạo đơn hàng mới",
            description = "Khách hàng tạo đơn hàng mới. Hệ thống sẽ kiểm tra tồn kho, trừ số lượng và trả về mã tracking để theo dõi đơn hàng"
    )
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    // API: Xem danh sách đơn hàng (Dành cho Admin)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    @Operation(
            summary = "Xem danh sách đơn hàng (Admin)",
            description = "Chỉ Admin mới truy cập được. Hỗ trợ lọc theo trạng thái đơn hàng, số điện thoại, và phân trang"
    )
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

    // API Admin đổi trạng thái đơn
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{id}/status")
    @Operation(
            summary = "Cập nhật trạng thái đơn hàng (Admin)",
            description = "Chỉ Admin mới truy cập được. Thay đổi trạng thái đơn hàng (PENDING, CONFIRMED, SHIPPING, COMPLETED CANCELED)"
    )
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status // Truyền status qua query param cho nhanh
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    // API tracking đơn hàng theo token (Dành cho khách hàng)
    @GetMapping("/track/{trackingToken}")
    @Operation(
            summary = "Tra cứu đơn hàng theo mã tracking",
            description = "Khách hàng có thể tra cứu trạng thái đơn hàng bằng mã tracking nhận được sau khi đặt hàng"
    )
    public ResponseEntity<OrderResponse> trackOrder(@PathVariable String trackingToken) {
        return ResponseEntity.ok(orderService.trackOrderByToken(trackingToken));
    }
}
