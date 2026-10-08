package com.printflow.dto.response;

import com.printflow.domain.enums.OrderStatus;

public record OrderStatusResponse(

        Long orderId,

        OrderStatus status

) {
}