package com.printflow.domain.enums;

public enum Role {
    CUSTOMER("ลูกค้า"),
    STAFF("พนักงาน"),
    ADMIN("ผู้ดูแลระบบ");

    // ชื่อภาษาไทยสำหรับแสดงในหน้าเว็บ (API ยังส่งเป็นชื่อ enum เหมือนเดิม)
    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
