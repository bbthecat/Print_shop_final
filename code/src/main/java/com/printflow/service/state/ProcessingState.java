package com.printflow.service.state;

import com.printflow.domain.enums.OrderStatus;

/** กำลังพิมพ์อยู่ — ยกเลิกไม่ได้แล้วเพราะใช้กระดาษ/หมึกไปแล้ว ไปต่อได้อย่างเดียว */
public class ProcessingState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PROCESSING;
    }

    @Override
    public OrderStatus ready() {
        return OrderStatus.READY;
    }
}