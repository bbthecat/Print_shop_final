package com.printflow.dto.request;

import com.printflow.domain.enums.PricingType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PrintServiceRequest(
        @NotBlank(message = "Service name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        String description,

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "Base price must be greater than or equal to 0")
        BigDecimal basePrice,

        @NotNull(message = "Pricing type is required")
        PricingType pricingType
) {
}
