package com.printflow.controller.web;

import com.printflow.config.SecurityConfig;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.response.OrderItemResponse;
import com.printflow.dto.response.OrderResponse;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.OrderCommandService;
import com.printflow.service.OrderQueryService;
import com.printflow.service.PromotionService;
import com.printflow.service.ServiceCatalogQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderWebController.class)
@Import(SecurityConfig.class)
class OrderWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderQueryService orderQueryService;

    @MockBean
    private OrderCommandService orderCommandService;

    @MockBean
    private ServiceCatalogQueryService catalogQueryService;

    @MockBean
    private PromotionService promotionService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    private OrderResponse sampleOrderResponse() {
        OrderItemResponse item = new OrderItemResponse(
                1L, 1L, 2, BigDecimal.valueOf(5.00), BigDecimal.valueOf(10.00), List.of()
        );
        return new OrderResponse(
                10L, "ORD-12345", 1L, OrderStatus.PENDING,
                BigDecimal.valueOf(10.00), LocalDateTime.now(), List.of(item)
        );
    }

    @Test
    @WithMockUser
    void showCreateForm_shouldReturnCreateViewAndModel() throws Exception {
        when(catalogQueryService.findAllActivePrintServices()).thenReturn(List.of());
        when(catalogQueryService.findAllActiveAddonServices()).thenReturn(List.of());
        when(promotionService.findAllActive()).thenReturn(List.of());

        mockMvc.perform(get("/orders/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/create"))
                .andExpect(model().attributeExists("services", "addons", "promotions", "form"));
    }

    @Test
    @WithMockUser
    void createOrder_shouldRedirectToDetailOnSuccess() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(orderCommandService.createOrder(any())).thenReturn(sampleOrderResponse());

        mockMvc.perform(post("/orders/create")
                        .with(csrf())
                        .param("serviceId", "1")
                        .param("quantity", "2")
                        .param("fileName", "test.pdf"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/10"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithMockUser
    void createOrder_shouldReturnCreateViewWhenValidationFails() throws Exception {
        when(catalogQueryService.findAllActivePrintServices()).thenReturn(List.of());
        when(catalogQueryService.findAllActiveAddonServices()).thenReturn(List.of());
        when(promotionService.findAllActive()).thenReturn(List.of());

        mockMvc.perform(post("/orders/create")
                        .with(csrf())
                        .param("quantity", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/create"));
    }

    @Test
    @WithMockUser
    void listOrders_shouldReturnHistoryViewAndModel() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(orderQueryService.getAllByUserId(any(), any()))
                .thenReturn(new PageImpl<>(List.of(sampleOrderResponse())));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/history"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    @WithMockUser
    void getOrderDetail_shouldReturnDetailViewAndModel() throws Exception {
        when(orderQueryService.getById(10L)).thenReturn(sampleOrderResponse());

        mockMvc.perform(get("/orders/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/detail"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    @WithMockUser
    void getOrderTracking_shouldReturnTrackingViewAndModel() throws Exception {
        when(orderQueryService.getById(10L)).thenReturn(sampleOrderResponse());

        mockMvc.perform(get("/orders/10/tracking"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/tracking"))
                .andExpect(model().attributeExists("order"));
    }
}
