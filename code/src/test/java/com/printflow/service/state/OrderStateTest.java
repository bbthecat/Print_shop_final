package com.printflow.service.state;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ทดสอบ State Pattern ครบทุกเส้นทาง
 * ทั้ง transition ที่อนุญาต และที่ต้องถูกปฏิเสธ
 */
class OrderStateTest {

    private final OrderStateResolver resolver = new OrderStateResolver();

    private OrderState state(OrderStatus status) {
        return resolver.resolve(status);
    }

    private void assertRejected(Runnable action) {
        assertThrows(InvalidStateTransitionException.class, action::run);
    }

    @Test
    @DisplayName("PENDING ยืนยันรับงานหรือยกเลิกได้")
    void pendingAllowsConfirmAndCancel() {
        assertEquals(OrderStatus.CONFIRMED, state(OrderStatus.PENDING).confirm());
        assertEquals(OrderStatus.CANCELLED, state(OrderStatus.PENDING).cancel());
    }

    @Test
    @DisplayName("PENDING ข้ามไปสถานะอื่นไม่ได้")
    void pendingRejectsOtherActions() {
        OrderState pending = state(OrderStatus.PENDING);
        assertRejected(pending::process);
        assertRejected(pending::ready);
        assertRejected(pending::complete);
    }

    @Test
    @DisplayName("CONFIRMED เริ่มงานหรือยกเลิกได้")
    void confirmedAllowsProcessAndCancel() {
        assertEquals(OrderStatus.PROCESSING, state(OrderStatus.CONFIRMED).process());
        assertEquals(OrderStatus.CANCELLED, state(OrderStatus.CONFIRMED).cancel());
    }

    @Test
    @DisplayName("CONFIRMED ยืนยันซ้ำหรือข้ามขั้นไม่ได้")
    void confirmedRejectsOtherActions() {
        OrderState confirmed = state(OrderStatus.CONFIRMED);
        assertRejected(confirmed::confirm);
        assertRejected(confirmed::ready);
        assertRejected(confirmed::complete);
    }

    @Test
    @DisplayName("PROCESSING ไปต่อได้อย่างเดียว ยกเลิกไม่ได้แล้ว")
    void processingOnlyAllowsReady() {
        assertEquals(OrderStatus.READY, state(OrderStatus.PROCESSING).ready());

        OrderState processing = state(OrderStatus.PROCESSING);
        assertRejected(processing::confirm);
        assertRejected(processing::process);
        assertRejected(processing::complete);
        assertRejected(processing::cancel);
    }

    @Test
    @DisplayName("READY ส่งมอบงานได้อย่างเดียว")
    void readyOnlyAllowsComplete() {
        assertEquals(OrderStatus.COMPLETED, state(OrderStatus.READY).complete());

        OrderState ready = state(OrderStatus.READY);
        assertRejected(ready::confirm);
        assertRejected(ready::process);
        assertRejected(ready::ready);
        assertRejected(ready::cancel);
    }

    @Test
    @DisplayName("COMPLETED เป็นสถานะสุดท้าย ทำอะไรต่อไม่ได้")
    void completedIsFinal() {
        OrderState completed = state(OrderStatus.COMPLETED);
        assertRejected(completed::confirm);
        assertRejected(completed::process);
        assertRejected(completed::ready);
        assertRejected(completed::complete);
        assertRejected(completed::cancel);
    }

    @Test
    @DisplayName("CANCELLED เป็นสถานะสุดท้าย ย้อนกลับไม่ได้")
    void cancelledIsFinal() {
        OrderState cancelled = state(OrderStatus.CANCELLED);
        assertRejected(cancelled::confirm);
        assertRejected(cancelled::process);
        assertRejected(cancelled::ready);
        assertRejected(cancelled::complete);
        assertRejected(cancelled::cancel);
    }

    @Test
    @DisplayName("ข้อความ error บอกทั้งสถานะเดิมและสถานะปลายทาง")
    void errorMessageNamesBothStatuses() {
        InvalidStateTransitionException ex = assertThrows(
                InvalidStateTransitionException.class,
                () -> state(OrderStatus.COMPLETED).process());

        assertEquals("Cannot change status from COMPLETED to PROCESSING", ex.getMessage());
    }

    @Test
    @DisplayName("ทุกค่าใน OrderStatus ต้องมีคลาสของสถานะรองรับ")
    void resolverCoversEveryStatus() {
        for (OrderStatus status : OrderStatus.values()) {
            assertEquals(status, resolver.resolve(status).getStatus());
        }
    }
}