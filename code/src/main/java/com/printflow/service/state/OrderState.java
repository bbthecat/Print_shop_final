package com.printflow.service.state;

import com.printflow.domain.enums.OrderStatus;

/**
 * State Pattern — สัญญาที่ทุกสถานะของคำสั่งซื้อต้องทำตาม
 *
 * แต่ละ method คือ action ที่สั่งกับ Order ได้ และคืนค่า "สถานะถัดไป"
 * ถ้าสถานะปัจจุบันไม่อนุญาต action นั้น จะโยน InvalidStateTransitionException
 * ซึ่ง GlobalExceptionHandler แปลงเป็น HTTP 409 Conflict ให้อยู่แล้ว
 */
public interface OrderState {

    /** สถานะที่คลาสนี้เป็นตัวแทน */
    OrderStatus getStatus();

    /** ร้านยืนยันรับงาน */
    OrderStatus confirm();

    /** เริ่มลงมือพิมพ์ */
    OrderStatus process();

    /** งานเสร็จ พร้อมให้มารับ */
    OrderStatus ready();

    /** ส่งมอบงานแล้ว */
    OrderStatus complete();

    /** ยกเลิกคำสั่งซื้อ */
    OrderStatus cancel();
}