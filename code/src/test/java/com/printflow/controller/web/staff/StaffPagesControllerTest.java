package com.printflow.controller.web.staff;

import com.printflow.config.SecurityConfig;
import com.printflow.domain.entity.Payment;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.domain.enums.PaymentMethod;
import com.printflow.dto.response.OrderItemResponse;
import com.printflow.dto.response.OrderResponse;
import com.printflow.dto.response.PaymentResponse;
import com.printflow.dto.response.ReportSummaryResponse;
import com.printflow.exception.InvalidStateTransitionException;
import com.printflow.mapper.PaymentMapper;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.OrderQueryService;
import com.printflow.service.OrderStatusService;
import com.printflow.service.PaymentService;
import com.printflow.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest({StaffDashboardController.class, StaffOrderDetailPageController.class, StaffPaymentPageController.class})
@Import(SecurityConfig.class)
class StaffPagesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private OrderQueryService orderQueryService;

    @MockBean
    private OrderStatusService orderStatusService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PaymentMapper paymentMapper;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    private OrderResponse order(OrderStatus status) {
        OrderItemResponse item = new OrderItemResponse(1L, 1L, "Document B&W (A4)", 20, 1,
                new BigDecimal("32.00"), new BigDecimal("32.00"), List.of(1L), List.of("Corner Staple"));
        return new OrderResponse(10L, "ORD-1", 7L, "somchai", status,
                new BigDecimal("1234.50"), LocalDateTime.now(), List.of(item));
    }

    private ReportSummaryResponse summary() {
        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus s : OrderStatus.values()) {
            counts.put(s, 0L);
        }
        counts.put(OrderStatus.PENDING, 4L);
        return new ReportSummaryResponse(LocalDate.now(), LocalDate.now(), 4, counts, BigDecimal.ZERO, List.of());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void dashboard_asCustomer_returns403() throws Exception {
        mockMvc.perform(get("/staff/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void dashboard_asStaff_showsPendingCount() throws Exception {
        when(reportService.getSummary(any(), any())).thenReturn(summary());
        when(reportService.getSummary(isNull(), isNull())).thenReturn(summary());

        mockMvc.perform(get("/staff/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff/dashboard"))
                .andExpect(content().string(containsString("รอร้านยืนยัน")));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void orderDetail_showsCustomerNameAndOnlyAllowedStatuses() throws Exception {
        when(orderQueryService.getById(10L)).thenReturn(order(OrderStatus.READY));
        when(orderStatusService.getHistories(10L)).thenReturn(List.of());
        when(orderStatusService.getAllowedNextStatuses(OrderStatus.READY)).thenReturn(List.of(OrderStatus.COMPLETED));

        mockMvc.perform(get("/staff/orders/10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("somchai")))
                .andExpect(content().string(containsString("1,234.50 บาท")))
                .andExpect(content().string(containsString("ส่งมอบแล้ว")))
                .andExpect(content().string(not(containsString("value=\"PROCESSING\""))));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void payments_cancelledOrder_hasNoPayButton() throws Exception {
        Payment payment = new Payment(10L, new BigDecimal("32.00"));
        PaymentResponse response = new PaymentResponse(1L, 10L, new BigDecimal("32.00"), null,
                payment.getPaymentStatus(), null);
        when(paymentService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(payment)));
        when(paymentMapper.toResponse(payment)).thenReturn(response);
        when(orderQueryService.getById(10L)).thenReturn(order(OrderStatus.CANCELLED));

        mockMvc.perform(get("/staff/payments"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ORD-1")))
                .andExpect(content().string(not(containsString("/staff/payments/10/paid"))));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void markPaid_twice_showsError() throws Exception {
        when(paymentService.markAsPaid(10L, PaymentMethod.CASH))
                .thenThrow(new InvalidStateTransitionException("Payment for order 10 is already PAID"));

        mockMvc.perform(post("/staff/payments/10/paid").with(csrf()).param("method", "CASH"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
    }
}
