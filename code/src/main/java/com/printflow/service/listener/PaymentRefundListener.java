package com.printflow.service.listener;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.service.PaymentService;
import com.printflow.service.event.OrderStatusChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observer: เมื่อ order ถูกยกเลิก ถ้าชำระเงินไปแล้วให้เปลี่ยน payment เป็น REFUNDED
 * รายงานยอดขายจะได้ไม่นับเงินของงานที่ยกเลิก
 */
@Component
public class PaymentRefundListener {

    private final PaymentService paymentService;

    public PaymentRefundListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @EventListener
    public void onStatusChanged(OrderStatusChangedEvent event) {
        if (event.newStatus() == OrderStatus.CANCELLED) {
            paymentService.refundIfPaid(event.orderId());
        }
    }
}
