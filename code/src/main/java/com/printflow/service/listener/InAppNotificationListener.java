package com.printflow.service.listener;

import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.repository.OrderRepository;
import com.printflow.service.NotificationService;
import com.printflow.service.event.OrderStatusChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class InAppNotificationListener {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public InAppNotificationListener(
            OrderRepository orderRepository,
            NotificationService notificationService
    ) {
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    @EventListener
    public void onStatusChanged(OrderStatusChangedEvent event) {
        PrintOrder order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null) {
            return;
        }

        String title = titleOf(event.newStatus());
        if (title == null) {
            return;
        }

        notificationService.create(
                order.getUserId(),
                order.getId(),
                title,
                messageOf(event.newStatus(), order.getOrderNumber())
        );
    }

    private String titleOf(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "ร้านรับงานแล้ว";
            case PROCESSING -> "กำลังดำเนินการ";
            case READY -> "งานเสร็จแล้ว";
            case COMPLETED -> "ส่งมอบงานแล้ว";
            case CANCELLED -> "คำสั่งซื้อถูกยกเลิก";
            case PENDING -> null;
        };
    }

    private String messageOf(OrderStatus status, String orderNumber) {
        return switch (status) {
            case CONFIRMED -> "คำสั่งซื้อ " + orderNumber + " ได้รับการยืนยันแล้ว ร้านจะเริ่มดำเนินการให้เร็วที่สุด";
            case PROCESSING -> "คำสั่งซื้อ " + orderNumber + " กำลังพิมพ์อยู่";
            case READY -> "คำสั่งซื้อ " + orderNumber + " พร้อมให้มารับที่ร้านแล้ว";
            case COMPLETED -> "คำสั่งซื้อ " + orderNumber + " ส่งมอบเรียบร้อย ขอบคุณที่ใช้บริการ";
            case CANCELLED -> "คำสั่งซื้อ " + orderNumber + " ถูกยกเลิกแล้ว";
            case PENDING -> "";
        };
    }
}