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

        List<Long> addonIds,

        @Min(1)
        Integer pageCount

) {
    public OrderItemRequest(Long serviceId, Integer quantity, List<Long> addonIds) {
        this(serviceId, quantity, addonIds, 1);
    }

    public OrderItemRequest(Long serviceId, Integer pageCount, Integer quantity, List<Long> addonIds) {
        this(serviceId, quantity, addonIds, pageCount != null ? pageCount : 1);
    }
}