package com.printflow.service.listener;

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
        paymentService.createUnpaidIfAbsent(event.orderId());
    }
}
