package com.printflow.service.state;

import com.printflow.domain.enums.OrderStatus;

/** ยกเลิกแล้ว — สถานะสุดท้าย ย้อนกลับไปสถานะอื่นไม่ได้ */
public class CancelledState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CANCELLED;
    }
}