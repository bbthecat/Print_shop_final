package com.printflow.dto.response;

import com.printflow.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ReportSummaryResponse(
        LocalDate from,
        LocalDate to,
        long totalOrders,
        Map<OrderStatus, Long> ordersByStatus,
        BigDecimal totalSales,
        List<TopServiceResponse> topServices
) {
}
