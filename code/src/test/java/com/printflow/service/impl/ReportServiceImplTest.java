package com.printflow.service.impl;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.domain.enums.PaymentStatus;
import com.printflow.dto.response.ReportSummaryResponse;
import com.printflow.exception.ValidationException;
import com.printflow.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private static ReportRepository.StatusCount statusCount(OrderStatus status, long total) {
        return new ReportRepository.StatusCount() {
            public OrderStatus getStatus() {
                return status;
            }

            public Long getTotal() {
                return total;
            }
        };
    }

    private static ReportRepository.ServiceUsage usage(String name, long quantity, String sales) {
        return new ReportRepository.ServiceUsage() {
            public String getServiceName() {
                return name;
            }

            public Long getTotalQuantity() {
                return quantity;
            }

            public BigDecimal getTotalSales() {
                return new BigDecimal(sales);
            }
        };
    }

    @Test
    void getSummary_buildsReportFromRepositoryData() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 7);
        when(reportRepository.countOrdersByStatus(any(), any())).thenReturn(List.of(
                statusCount(OrderStatus.PENDING, 3), statusCount(OrderStatus.COMPLETED, 2)));
        when(reportRepository.sumPaymentAmount(eq(PaymentStatus.PAID), any(), any()))
                .thenReturn(new BigDecimal("500.00"));
        when(reportRepository.findTopServices(eq(OrderStatus.CANCELLED), any(), any(), any()))
                .thenReturn(List.of(usage("พิมพ์สี", 40, "400.00")));

        ReportSummaryResponse report = reportService.getSummary(from, to);

        assertEquals(from, report.from());
        assertEquals(to, report.to());
        assertEquals(5, report.totalOrders());
        assertEquals(3L, report.ordersByStatus().get(OrderStatus.PENDING));
        assertEquals(new BigDecimal("500.00"), report.totalSales());
        assertEquals(1, report.topServices().size());
        assertEquals("พิมพ์สี", report.topServices().get(0).serviceName());
    }

    @Test
    void getSummary_statusWithoutOrders_showsZero() {
        when(reportRepository.countOrdersByStatus(any(), any()))
                .thenReturn(List.of(statusCount(OrderStatus.PENDING, 1)));

        ReportSummaryResponse report = reportService.getSummary(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 1));

        assertEquals(OrderStatus.values().length, report.ordersByStatus().size());
        assertEquals(0L, report.ordersByStatus().get(OrderStatus.CANCELLED));
    }

    @Test
    void getSummary_noPaidPayments_totalSalesIsZero() {
        when(reportRepository.sumPaymentAmount(any(), any(), any())).thenReturn(null);

        ReportSummaryResponse report = reportService.getSummary(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 1));

        assertEquals(BigDecimal.ZERO, report.totalSales());
    }

    @Test
    void getSummary_includesWholeLastDay() {
        reportService.getSummary(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 7));

        verify(reportRepository).countOrdersByStatus(
                LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 10, 8, 0, 0));
    }

    @Test
    void getSummary_withoutDates_usesLast30Days() {
        ReportSummaryResponse report = reportService.getSummary(null, null);

        assertEquals(LocalDate.now(), report.to());
        assertEquals(LocalDate.now().minusDays(29), report.from());
    }

    @Test
    void getSummary_fromAfterTo_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> reportService.getSummary(LocalDate.of(2026, 10, 7), LocalDate.of(2026, 10, 1)));
        verifyNoInteractions(reportRepository);
    }
}
