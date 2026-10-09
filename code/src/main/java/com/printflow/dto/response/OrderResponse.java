package com.printflow.dto.response;

import com.printflow.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(

        Long id,

        String orderNumber,

        Long userId,
        String customerName,

        OrderStatus status,

        BigDecimal totalPrice,

        LocalDateTime createdAt,

        List<OrderItemResponse> items,

        // ส่วนลดจากโปรโมชัน (0 ถ้าไม่ได้ใช้โค้ด)
        BigDecimal discountAmount,
        String promotionCode

) {
    public OrderResponse(Long id, String orderNumber, Long userId, String customerName, OrderStatus status,
                         BigDecimal totalPrice, LocalDateTime createdAt, List<OrderItemResponse> items) {
        this(id, orderNumber, userId, customerName, status, totalPrice, createdAt, items, BigDecimal.ZERO, null);
    }

    // ยอดก่อนหักส่วนลด
    public BigDecimal subtotal() {
        return totalPrice.add(discountAmount);
    }
}