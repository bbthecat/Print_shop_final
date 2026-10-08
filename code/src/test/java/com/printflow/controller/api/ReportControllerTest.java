package com.printflow.controller.api;

import com.printflow.config.SecurityConfig;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.response.ReportSummaryResponse;
import com.printflow.dto.response.TopServiceResponse;
import com.printflow.exception.ValidationException;
import com.printflow.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)
class ReportControllerTest {

    private static final LocalDate FROM = LocalDate.of(2026, 10, 1);
    private static final LocalDate TO = LocalDate.of(2026, 10, 7);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    private ReportSummaryResponse sampleReport() {
        return new ReportSummaryResponse(FROM, TO, 5,
                Map.of(OrderStatus.PENDING, 3L, OrderStatus.COMPLETED, 2L),
                new BigDecimal("500.00"),
                List.of(new TopServiceResponse("พิมพ์สี", 40L, new BigDecimal("400.00"))));
    }

    @Test
    void summary_anonymous_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/reports/summary"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(reportService);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void summary_asStaff_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/reports/summary"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(reportService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void summary_asAdmin_returnsReport() throws Exception {
        when(reportService.getSummary(FROM, TO)).thenReturn(sampleReport());

        mockMvc.perform(get("/api/v1/reports/summary").param("from", "2026-10-01").param("to", "2026-10-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("2026-10-01"))
                .andExpect(jsonPath("$.totalOrders").value(5))
                .andExpect(jsonPath("$.ordersByStatus.PENDING").value(3))
                .andExpect(jsonPath("$.totalSales").value(500.00))
                .andExpect(jsonPath("$.topServices[0].serviceName").value("พิมพ์สี"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void summary_invalidRange_returns400() throws Exception {
        when(reportService.getSummary(TO, FROM))
                .thenThrow(new ValidationException("วันที่เริ่มต้นต้องไม่อยู่หลังวันที่สิ้นสุด"));

        mockMvc.perform(get("/api/v1/reports/summary").param("from", "2026-10-07").param("to", "2026-10-01"))
                .andExpect(status().isBadRequest());
    }
}
