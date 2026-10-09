package com.printflow.domain.enums;

public enum OrderStatus {
    PENDING("รอร้านยืนยัน"),
    CONFIRMED("ร้านรับงานแล้ว"),
    PROCESSING("กำลังพิมพ์"),
    READY("พร้อมรับงาน"),
    COMPLETED("ส่งมอบแล้ว"),
    CANCELLED("ยกเลิกแล้ว");

    // ชื่อภาษาไทยสำหรับแสดงในหน้าเว็บ (API ยังส่งเป็นชื่อ enum เหมือนเดิม)
    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
