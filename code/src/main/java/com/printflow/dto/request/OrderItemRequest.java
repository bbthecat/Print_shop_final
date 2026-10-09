package com.printflow.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderItemRequest(

        @NotNull
        Long serviceId,

        // จำนวนหน้าต่อ 1 ชุด
        @NotNull
        @Min(1)
        Integer pageCount,

        // จำนวนชุด
        @NotNull
        @Min(1)
        Integer quantity,

        List<Long> addonIds

) {
}
