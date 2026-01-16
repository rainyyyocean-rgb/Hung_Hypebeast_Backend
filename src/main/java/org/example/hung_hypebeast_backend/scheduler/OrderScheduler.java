package org.example.hung_hypebeast_backend.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.service.impl.OrderServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderServiceImpl orderService;

    // Chạy mỗi 1 phút một lần
    @Scheduled(fixedRate = 60000)
    public void scanExpiredOrders() {
        orderService.cancelUnpaidOrders();
    }
}
