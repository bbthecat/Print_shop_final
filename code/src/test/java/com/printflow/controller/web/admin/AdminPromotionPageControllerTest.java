package com.printflow.controller.web.admin;

import com.printflow.config.SecurityConfig;
import com.printflow.domain.entity.Promotion;
import com.printflow.domain.enums.DiscountType;
import com.printflow.service.PromotionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminPromotionPageController.class)
@Import(SecurityConfig.class)
class AdminPromotionPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionService promotionService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_whenAdmin_shouldReturnView() throws Exception {
        Promotion promo = new Promotion();
        promo.setId(1L);
        promo.setCode("TEST10");
        promo.setDiscountType(DiscountType.PERCENTAGE);
        promo.setDiscountValue(new BigDecimal("10.00"));
        promo.setMinOrderAmount(BigDecimal.ZERO);
        promo.setStartDate(LocalDateTime.now().minusDays(1));
        promo.setEndDate(LocalDateTime.now().plusDays(10));
        promo.setActive(true);

        when(promotionService.findAllActive()).thenReturn(List.of(promo));

        mockMvc.perform(get("/admin/promotions"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/promotions"))
                .andExpect(model().attributeExists("promotions", "form"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_whenValid_shouldRedirect() throws Exception {
        mockMvc.perform(post("/admin/promotions")
                        .with(csrf())
                        .param("code", "SAVE50")
                        .param("description", "Save 50 baht")
                        .param("discountType", "FIXED_AMOUNT")
                        .param("discountValue", "50.00")
                        .param("minOrderAmount", "200.00")
                        .param("startDate", "2026-10-01T00:00:00")
                        .param("endDate", "2026-10-31T23:59:59"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/promotions"))
                .andExpect(flash().attributeExists("success"));

        verify(promotionService).create(
                eq("SAVE50"),
                eq("Save 50 baht"),
                eq(DiscountType.FIXED_AMOUNT),
                eq(new BigDecimal("50.00")),
                eq(new BigDecimal("200.00")),
                eq(LocalDateTime.parse("2026-10-01T00:00:00")),
                eq(LocalDateTime.parse("2026-10-31T23:59:59"))
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_whenInvalid_shouldReturnViewWithErrors() throws Exception {
        mockMvc.perform(post("/admin/promotions")
                        .with(csrf())
                        .param("code", "")
                        .param("discountValue", "-5.00"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/promotions"))
                .andExpect(model().attributeExists("promotions", "openForm"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivate_whenAdmin_shouldRedirect() throws Exception {
        mockMvc.perform(post("/admin/promotions/1/deactivate")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/promotions"))
                .andExpect(flash().attributeExists("success"));

        verify(promotionService).deactivate(1L);
    }

    @Test
    void list_whenAnonymous_shouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/admin/promotions"))
                .andExpect(status().isUnauthorized());
    }
}
