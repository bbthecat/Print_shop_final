package com.printflow.service.impl;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.domain.enums.PaymentStatus;
import com.printflow.dto.response.ReportSummaryResponse;
import com.printflow.dto.response.TopServiceResponse;
import com.printflow.exception.ValidationException;
import com.printflow.repository.ReportRepository;
import com.printflow.service.ReportService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final int DEFAULT_DAYS = 30;
    private static final int TOP_SERVICE_LIMIT = 5;

    private final ReportRepository reportRepository;

    public ReportServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public ReportSummaryResponse getSummary(LocalDate from, LocalDate to) {
        LocalDate endDate = (to != null) ? to : LocalDate.now();
        LocalDate startDate = (from != null) ? from : endDate.minusDays(DEFAULT_DAYS - 1);
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("วันที่เริ่มต้นต้องไม่อยู่หลังวันที่สิ้นสุด");
        }

        // ช่วงเวลา [วันเริ่ม 00:00, วันถัดจากวันสิ้นสุด 00:00) จะได้รวมทั้งวันสุดท้าย
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        Map<OrderStatus, Long> ordersByStatus = countOrdersByStatus(start, end);
        long totalOrders = ordersByStatus.values().stream().mapToLong(Long::longValue).sum();

        // ยอดขาย = เงินที่ชำระแล้ว (PAID) ในช่วงวันที่เลือก
        BigDecimal totalSales = reportRepository.sumPaymentAmount(PaymentStatus.PAID, start, end);
        if (totalSales == null) {
            totalSales = BigDecimal.ZERO;
        }

        // บริการยอดนิยม ไม่นับคำสั่งที่ถูกยกเลิก
        List<TopServiceResponse> topServices = reportRepository
                .findTopServices(OrderStatus.CANCELLED, start, end, PageRequest.of(0, TOP_SERVICE_LIMIT))
                .stream()
                .map(row -> new TopServiceResponse(row.getServiceName(), row.getTotalQuantity(), row.getTotalSales()))
                .toList();

        return new ReportSummaryResponse(startDate, endDate, totalOrders, ordersByStatus, totalSales, topServices);
    }

    // ใส่ 0 ให้ทุกสถานะก่อน สถานะที่ไม่มีคำสั่งจะได้แสดงเป็น 0
    private Map<OrderStatus, Long> countOrdersByStatus(LocalDateTime start, LocalDateTime end) {
        Map<OrderStatus, Long> result = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            result.put(status, 0L);
        }
        for (ReportRepository.StatusCount row : reportRepository.countOrdersByStatus(start, end)) {
            result.put(row.getStatus(), row.getTotal());
        }
        return result;
    }
}
