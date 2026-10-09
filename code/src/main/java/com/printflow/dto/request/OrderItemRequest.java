package com.printflow.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderItemRequest(

        @NotNull
        Long serviceId,

        // จำนวนหน้าต่อ 1 ชุด
        @NotNull
        @Min(1)
        @Max(2000)
        Integer pageCount,

        // จำนวนชุด
        @NotNull
        @Min(1)
        @Max(1000)
        Integer quantity,

        List<Long> addonIds

) {
}
