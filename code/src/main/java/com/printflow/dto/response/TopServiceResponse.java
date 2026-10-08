package com.printflow.dto.response;

import java.math.BigDecimal;

public record TopServiceResponse(
        String serviceName,
        Long totalQuantity,
        BigDecimal totalSales
) {
}
