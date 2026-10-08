package com.printflow.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderItemRequest(

        @NotNull
        Long serviceId,

        @NotNull
        @Min(1)
        Integer quantity,

        List<Long> addonIds

) {
}