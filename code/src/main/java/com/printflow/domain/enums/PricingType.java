package com.printflow.domain.enums;

public enum PricingType {
    BLACK_WHITE("ขาวดำ"),
    COLOR("สี"),
    PHOTO("ภาพถ่าย");

    // ชื่อภาษาไทยสำหรับแสดงในหน้าเว็บ (API ยังส่งเป็นชื่อ enum เหมือนเดิม)
    private final String label;

    PricingType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
