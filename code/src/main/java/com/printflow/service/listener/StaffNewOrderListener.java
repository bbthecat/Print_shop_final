package com.printflow.service.listener;

import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.entity.User;
import com.printflow.domain.enums.Role;
import com.printflow.repository.OrderRepository;
import com.printflow.repository.UserRepository;
import com.printflow.service.NotificationService;
import com.printflow.service.event.OrderCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observer: มีคำสั่งพิมพ์ใหม่ → แจ้งเตือนพนักงาน (STAFF) ทุกคนที่ยังใช้งานอยู่
 */
@Component
public class StaffNewOrderListener {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public StaffNewOrderListener(
            OrderRepository orderRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        PrintOrder order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null) {
            return;
        }
        for (User staff : userRepository.findAllByRoleAndActiveTrue(Role.STAFF)) {
            notificationService.create(
                    staff.getId(),
                    order.getId(),
                    "มีคำสั่งพิมพ์ใหม่",
                    "คำสั่งซื้อ " + order.getOrderNumber() + " ยอด " + order.getTotalPrice() + " บาท รอร้านยืนยัน"
            );
        }
    }
}
