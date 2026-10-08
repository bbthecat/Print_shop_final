package com.printflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AddonServiceResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
