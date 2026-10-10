package com.printflow.domain.enums;

public enum PaymentStatus {
    UNPAID("ยังไม่ชำระ"),
    PAID("ชำระแล้ว"),
    REFUNDED("คืนเงินแล้ว");

    // ชื่อภาษาไทยสำหรับแสดงในหน้าเว็บ (API ยังส่งเป็นชื่อ enum เหมือนเดิม)
    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
