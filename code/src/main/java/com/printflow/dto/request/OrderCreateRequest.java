package com.printflow.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

// ไม่มี userId: เจ้าของ order คือคนที่ login อยู่เสมอ (ส่งเข้า service จาก controller)
public record OrderCreateRequest(

        @NotEmpty
        List<@Valid OrderItemRequest> items,

        String promotionCode,

        // ไฟล์งาน (ไม่บังคับ): ชื่อไฟล์ เช่น report.pdf ใช้ตรวจชนิดไฟล์ใน validation chain
        @Size(max = 255)
        String fileName,

        // ลิงก์ไฟล์ เช่น Google Drive (ไม่บังคับ) ถ้าไม่ใส่ = นำไฟล์มาที่ร้าน
        @Size(max = 500)
        String fileUrl

) {
}
