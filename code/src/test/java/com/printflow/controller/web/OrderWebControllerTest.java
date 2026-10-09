package com.printflow.controller.web;

import com.printflow.config.SecurityConfig;
import com.printflow.domain.entity.Promotion;
import com.printflow.domain.enums.DiscountType;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.response.OrderItemResponse;
import com.printflow.dto.response.OrderResponse;
import com.printflow.exception.InvalidStateTransitionException;
import com.printflow.exception.ValidationException;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.OrderCommandService;
import com.printflow.service.OrderQueryService;
import com.printflow.service.OrderStatusService;
import com.printflow.service.PromotionService;
import com.printflow.service.ServiceCatalogQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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

    @MockBean
    private OrderStatusService orderStatusService;

    private OrderResponse sampleOrderResponse() {
        OrderItemResponse item = new OrderItemResponse(
                1L, 1L, "Document B&W (A4)", 20, 2, BigDecimal.valueOf(5.00), BigDecimal.valueOf(10.00),
                List.of(1L), List.of("Corner Staple")
        );
        return new OrderResponse(
                10L, "ORD-12345", 1L, "customer01", OrderStatus.PENDING,
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
                .andExpect(model().attributeExists("services", "addons", "promotions", "form"))
                // ช่องเลือกไฟล์ไว้นับหน้าในเบราว์เซอร์ ไม่มี name จึงไม่ส่งไฟล์ขึ้น server
                .andExpect(content().string(containsString("<input type=\"file\" id=\"filePicker\" accept=\".pdf,.jpg,.jpeg,.png\">")))
                .andExpect(content().string(containsString("pdf.js/3.11.174/")));
    }

    @Test
    @WithMockUser
    void showCreateForm_withActivePromotion_rendersPromotionCode() throws Exception {
        Promotion promo = new Promotion();
        promo.setCode("WELCOME10");
        promo.setDiscountType(DiscountType.PERCENTAGE);
        promo.setDiscountValue(BigDecimal.TEN);
        promo.setMinOrderAmount(BigDecimal.ZERO);
        when(catalogQueryService.findAllActivePrintServices()).thenReturn(List.of());
        when(catalogQueryService.findAllActiveAddonServices()).thenReturn(List.of());
        when(promotionService.findAllActive()).thenReturn(List.of(promo));

        // Thymeleaf 3.1 ห้ามใส่ string ใน th:onclick ถ้าใช้ผิดหน้านี้จะ error 500
        mockMvc.perform(get("/orders/create"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-code=\"WELCOME10\"")));
    }

    @Test
    @WithMockUser
    void createOrder_shouldRedirectToDetailOnSuccess() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(orderCommandService.createOrder(eq(1L), any())).thenReturn(sampleOrderResponse());

        mockMvc.perform(post("/orders/create")
                        .with(csrf())
                        .param("serviceId", "1")
                        .param("pageCount", "20")
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
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(orderQueryService.getByIdForUser(10L, 1L, false)).thenReturn(sampleOrderResponse());

        mockMvc.perform(get("/orders/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/detail"))
                .andExpect(model().attributeExists("order"))
                .andExpect(content().string(containsString("Document B&amp;W (A4)")))
                .andExpect(content().string(containsString("Corner Staple")));
    }

    @Test
    @WithMockUser
    void getOrderDetail_otherCustomersOrder_returns403() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(2L);
        when(orderQueryService.getByIdForUser(10L, 2L, false))
                .thenThrow(new AccessDeniedException("You can only view your own orders"));

        mockMvc.perform(get("/orders/10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getOrderDetail_nonNumericId_returns400ErrorPage() throws Exception {
        mockMvc.perform(get("/orders/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error"));
    }

    @Test
    @WithMockUser
    void getOrderTracking_shouldReturnTrackingViewAndModel() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(orderQueryService.getByIdForUser(10L, 1L, false)).thenReturn(sampleOrderResponse());

        mockMvc.perform(get("/orders/10/tracking"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/tracking"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    @WithMockUser
    void cancelOrder_ownPendingOrder_redirectsWithSuccess() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        mockMvc.perform(post("/orders/10/cancel").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/10"))
                .andExpect(flash().attributeExists("success"));

        verify(orderStatusService).cancelByCustomer(10L, 1L);
    }

    @Test
    @WithMockUser
    void cancelOrder_alreadyConfirmed_showsError() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(orderStatusService.cancelByCustomer(10L, 1L))
                .thenThrow(new InvalidStateTransitionException("ยกเลิกได้เฉพาะคำสั่งที่ยังรอร้านยืนยัน (PENDING) เท่านั้น"));

        mockMvc.perform(post("/orders/10/cancel").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser
    void createOrder_serviceError_keepsInputOnForm() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(catalogQueryService.findAllActivePrintServices()).thenReturn(List.of());
        when(catalogQueryService.findAllActiveAddonServices()).thenReturn(List.of());
        when(promotionService.findAllActive()).thenReturn(List.of());
        when(orderCommandService.createOrder(eq(1L), any()))
                .thenThrow(new ValidationException("ยอดสั่งซื้อยังไม่ถึงขั้นต่ำ"));

        mockMvc.perform(post("/orders/create")
                        .with(csrf())
                        .param("serviceId", "1")
                        .param("pageCount", "20")
                        .param("quantity", "1")
                        .param("promotionCode", "SAVE30"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/create"))
                .andExpect(model().attribute("error", "ยอดสั่งซื้อยังไม่ถึงขั้นต่ำ"))
                .andExpect(content().string(containsString("SAVE30")));
    }
}
