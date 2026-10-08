package com.printflow.service.state;

import com.printflow.domain.enums.OrderStatus;

/** รอร้านตรวจสอบ — ร้านยืนยันรับงานได้ หรือยกเลิกได้ */
public class PendingState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PENDING;
    }

    @Override
    public OrderStatus confirm() {
        return OrderStatus.CONFIRMED;
    }

    @Override
    public OrderStatus cancel() {
        return OrderStatus.CANCELLED;
    }
}
