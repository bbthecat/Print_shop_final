package com.printflow.service.state;

import com.printflow.domain.enums.OrderStatus;

/** งานเสร็จ รอลูกค้ามารับ — เหลือขั้นตอนเดียวคือส่งมอบ */
public class ReadyState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.READY;
    }

    @Override
    public OrderStatus complete() {
        return OrderStatus.COMPLETED;
    }
}
