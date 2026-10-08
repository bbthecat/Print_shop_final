package com.printflow.dto.response;

import com.printflow.domain.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromotionResponse(
        Long id,
        String code,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minOrderAmount,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
