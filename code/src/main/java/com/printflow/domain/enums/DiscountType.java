package com.printflow.domain.enums;

public enum DiscountType {
    PERCENTAGE("ลดเป็นเปอร์เซ็นต์"),
    FIXED_AMOUNT("ลดเป็นจำนวนเงิน");

    // ชื่อภาษาไทยสำหรับแสดงในหน้าเว็บ (API ยังส่งเป็นชื่อ enum เหมือนเดิม)
    private final String label;

    DiscountType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
