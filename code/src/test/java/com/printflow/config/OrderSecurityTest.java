package com.printflow.config;

import com.printflow.controller.api.OrderController;
import com.printflow.controller.api.OrderStatusController;
import com.printflow.controller.api.PaymentController;
import com.printflow.mapper.PaymentMapper;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.OrderCommandService;
import com.printflow.service.OrderQueryService;
import com.printflow.service.OrderStatusService;
import com.printflow.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks the SecurityConfig rules for orders: customers can place orders,
 * but only STAFF/ADMIN can see every order, change status or record payments.
 */
@WebMvcTest({OrderController.class, OrderStatusController.class, PaymentController.class})
@Import(SecurityConfig.class)
class OrderSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderCommandService commandService;

    @MockBean
    private OrderQueryService queryService;

    @MockBean
    private OrderStatusService orderStatusService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PaymentMapper paymentMapper;

    @Test
    void listOrders_anonymous_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void listAllOrders_asCustomer_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(queryService);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void listAllOrders_asStaff_isAllowed() throws Exception {
        when(queryService.getAll(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void changeStatus_asCustomer_returns403() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(orderStatusService);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void markPaid_asCustomer_returns403() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/1/payment")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"paymentMethod\":\"CASH\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createPayment_asCustomer_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/orders/1/payment"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void deleteOrder_asStaff_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/1"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(commandService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteOrder_asAdmin_isAllowed() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/1"))
                .andExpect(status().isNoContent());
        verify(commandService).deleteOrder(1L);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void statusHistoryOfOtherCustomersOrder_returns403() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(2L);
        when(queryService.getByIdForUser(1L, 2L, false))
                .thenThrow(new AccessDeniedException("You can only view your own orders"));

        mockMvc.perform(get("/api/v1/orders/1/status-histories"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(orderStatusService);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void paymentOfOtherCustomersOrder_returns403() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(2L);
        when(queryService.getByIdForUser(1L, 2L, false))
                .thenThrow(new AccessDeniedException("You can only view your own orders"));

        mockMvc.perform(get("/api/v1/orders/1/payment"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(paymentService);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCancelsOwnOrder_isAllowed() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(2L);
        when(orderStatusService.cancelByCustomer(1L, 2L)).thenReturn(com.printflow.domain.enums.OrderStatus.CANCELLED);

        mockMvc.perform(post("/api/v1/orders/1/cancel"))
                .andExpect(status().isOk());
    }
}
