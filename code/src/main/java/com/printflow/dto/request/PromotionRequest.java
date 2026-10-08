package com.printflow.dto.request;

import com.printflow.domain.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromotionRequest(
        @NotBlank(message = "Promotion code is required")
        @Size(max = 50, message = "Code must not exceed 50 characters")
        String code,

        String description,

        @NotNull(message = "Discount type is required")
        DiscountType discountType,

        @NotNull(message = "Discount value is required")
        @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
        BigDecimal discountValue,

        @DecimalMin(value = "0.00", inclusive = true, message = "Minimum order amount must be greater than or equal to 0")
        BigDecimal minOrderAmount,

        @NotNull(message = "Start date is required")
        LocalDateTime startDate,

        @NotNull(message = "End date is required")
        LocalDateTime endDate
) {
}
