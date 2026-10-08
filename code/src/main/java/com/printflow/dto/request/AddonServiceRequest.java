package com.printflow.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AddonServiceRequest(
        @NotBlank(message = "Addon name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "Price must be greater than or equal to 0")
        BigDecimal price
) {
}
