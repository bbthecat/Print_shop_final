package com.printflow.service.event;

import com.printflow.domain.enums.OrderStatus;

public record OrderStatusChangedEvent(
        Long orderId,
        OrderStatus oldStatus,
        OrderStatus newStatus,
        Long changedBy
) {
}
