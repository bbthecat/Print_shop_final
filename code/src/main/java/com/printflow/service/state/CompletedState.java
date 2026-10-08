package com.printflow.service.state;

import com.printflow.domain.enums.OrderStatus;

/**
 * ส่งมอบงานแล้ว — สถานะสุดท้าย ไม่อนุญาต action ใดอีก
 * ใช้ค่าตั้งต้นจาก AbstractOrderState ทั้งหมด (ทุก action โยน 409)
 */
public class CompletedState extends AbstractOrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.COMPLETED;
    }
}