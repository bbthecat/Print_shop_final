package com.printflow.service.state;

import com.printflow.domain.enums.OrderStatus;

/** ร้านรับงานแล้ว — เริ่มงานได้ หรือยังยกเลิกได้ถ้ายังไม่ลงมือ */
public class ConfirmedState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CONFIRMED;
    }

    @Override
    public OrderStatus process() {
        return OrderStatus.PROCESSING;
    }

    @Override
    public OrderStatus cancel() {
        return OrderStatus.CANCELLED;
    }
}