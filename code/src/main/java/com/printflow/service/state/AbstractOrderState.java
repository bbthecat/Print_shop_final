package com.printflow.service.state;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.exception.InvalidStateTransitionException;

/**
 * ค่าตั้งต้นของทุกสถานะคือ "ไม่อนุญาตให้ทำอะไรเลย"
 * คลาสลูกจะ override เฉพาะ action ที่สถานะของตัวเองอนุญาตเท่านั้น
 *
 * ผลคือกฎการเปลี่ยนสถานะไปอยู่ในคลาสของแต่ละสถานะ
 * แทนที่จะเป็น if-else ตรวจ status ก้อนใหญ่กระจายอยู่ใน Service
 * เพิ่มสถานะใหม่ = เพิ่มคลาสใหม่ ไม่ต้องแก้ของเดิม (Open/Closed Principle)
 */
public abstract class AbstractOrderState implements OrderState {

    @Override
    public OrderStatus confirm() {
        return reject(OrderStatus.CONFIRMED);
    }

    @Override
    public OrderStatus process() {
        return reject(OrderStatus.PROCESSING);
    }

    @Override
    public OrderStatus ready() {
        return reject(OrderStatus.READY);
    }

    @Override
    public OrderStatus complete() {
        return reject(OrderStatus.COMPLETED);
    }

    @Override
    public OrderStatus cancel() {
        return reject(OrderStatus.CANCELLED);
    }

    /**
     * ปฏิเสธการเปลี่ยนสถานะที่ผิดกฎ
     * ข้อความ error ตรงกับรูปแบบที่ระบุไว้ใน Project Proposal
     */
    protected OrderStatus reject(OrderStatus target) {
        throw new InvalidStateTransitionException(
                "Cannot change status from " + getStatus() + " to " + target);
    }
}
