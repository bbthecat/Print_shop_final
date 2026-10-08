package com.printflow.dto.request;

import com.printflow.domain.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(

        @NotNull
        OrderStatus status

) {
}