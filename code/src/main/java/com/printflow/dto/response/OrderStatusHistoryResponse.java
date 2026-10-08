package com.printflow.dto.response;

import com.printflow.domain.enums.OrderStatus;

import java.time.LocalDateTime;

public record OrderStatusHistoryResponse(

        Long id,

        OrderStatus oldStatus,

        OrderStatus newStatus,

        Long changedBy,

        LocalDateTime changedAt

) {
}