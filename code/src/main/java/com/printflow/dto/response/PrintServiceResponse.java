package com.printflow.dto.response;

import com.printflow.domain.enums.PricingType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PrintServiceResponse(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        PricingType pricingType,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
