package com.printflow.controller.web.admin;

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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminReportPageController.class)
@Import(SecurityConfig.class)
class AdminReportPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    private ReportSummaryResponse sampleReport() {
        return new ReportSummaryResponse(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 7), 5,
                Map.of(OrderStatus.PENDING, 3L, OrderStatus.COMPLETED, 2L),
                new BigDecimal("1500.00"),
                List.of(new TopServiceResponse("พิมพ์สี", 40L, new BigDecimal("400.00"))));
    }

    @Test
    void reportsPage_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/reports").accept("text/html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void reportsPage_asStaff_returns403() throws Exception {
        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reportsPage_asAdmin_rendersSummary() throws Exception {
        when(reportService.getSummary(isNull(), isNull())).thenReturn(sampleReport());

        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reports"))
                .andExpect(content().string(containsString("1,500.00 บาท")))
                .andExpect(content().string(containsString("พิมพ์สี")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reportsPage_invalidRange_showsErrorAndDefaultReport() throws Exception {
        when(reportService.getSummary(LocalDate.of(2026, 10, 7), LocalDate.of(2026, 10, 1)))
                .thenThrow(new ValidationException("วันที่เริ่มต้นต้องไม่อยู่หลังวันที่สิ้นสุด"));
        when(reportService.getSummary(isNull(), isNull())).thenReturn(sampleReport());

        mockMvc.perform(get("/admin/reports").param("from", "2026-10-07").param("to", "2026-10-01"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "วันที่เริ่มต้นต้องไม่อยู่หลังวันที่สิ้นสุด"))
                .andExpect(content().string(containsString("วันที่เริ่มต้นต้องไม่อยู่หลังวันที่สิ้นสุด")));
    }
}
