package com.printflow.service.listener;

import com.printflow.exception.DuplicateResourceException;
import com.printflow.service.PaymentService;
import com.printflow.service.event.OrderCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentCreationListener {

    private final PaymentService paymentService;

    public PaymentCreationListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        try {
            paymentService.createUnpaid(event.orderId());
        } catch (DuplicateResourceException ex) {
            // payment มีอยู่แล้ว ไม่ต้องสร้างซ้ำ
        }
    }
}