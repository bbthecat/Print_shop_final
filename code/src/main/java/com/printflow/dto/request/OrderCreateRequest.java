package com.printflow.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// ไม่มี userId: เจ้าของ order คือคนที่ login อยู่เสมอ (ส่งเข้า service จาก controller)
public record OrderCreateRequest(

        @NotEmpty
        List<@Valid OrderItemRequest> items,

        String promotionCode

) {
}
