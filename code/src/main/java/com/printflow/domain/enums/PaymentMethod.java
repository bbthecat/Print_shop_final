package com.printflow.domain.enums;

public enum PaymentMethod {
    CASH("เงินสด"),
    TRANSFER("โอนเงิน"),
    QR("QR พร้อมเพย์");

    // ชื่อภาษาไทยสำหรับแสดงในหน้าเว็บ (API ยังส่งเป็นชื่อ enum เหมือนเดิม)
    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
