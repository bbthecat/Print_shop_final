package com.printflow.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequest(

        @NotNull
        Long userId,

        @NotEmpty
        List<@Valid OrderItemRequest> items,

        String promotionCode

) {
}