package com.printflow.dto.response;

import com.printflow.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(

        Long id,

        String orderNumber,

        Long userId,

        OrderStatus status,

        BigDecimal totalPrice,

        LocalDateTime createdAt,

        List<OrderItemResponse> items

) {
}