package com.printflow.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record OrderItemResponse(
        Long id,
        Long serviceId,
        String serviceName,
        Integer pageCount,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        List<Long> addonIds,
        List<String> addonNames
) {
}
