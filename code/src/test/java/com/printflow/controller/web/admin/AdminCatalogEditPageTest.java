package com.printflow.controller.web.admin;

import com.printflow.config.SecurityConfig;
import com.printflow.domain.entity.PrintService;
import com.printflow.domain.entity.Promotion;
import com.printflow.domain.enums.DiscountType;
import com.printflow.domain.enums.PricingType;
import com.printflow.service.PromotionService;
import com.printflow.service.ServiceCatalogCommandService;
import com.printflow.service.ServiceCatalogQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest({AdminServicePageController.class, AdminPromotionPageController.class})
@Import(SecurityConfig.class)
class AdminCatalogEditPageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceCatalogQueryService queryService;

    @MockBean
    private ServiceCatalogCommandService commandService;

    @MockBean
    private PromotionService promotionService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void editServiceForm_showsCurrentValues() throws Exception {
        PrintService service = new PrintService();
        service.setId(1L);
        service.setName("Document B&W (A4)");
        service.setBasePrice(new BigDecimal("1.50"));
        service.setPricingType(PricingType.BLACK_WHITE);
        when(queryService.findPrintServiceById(1L)).thenReturn(service);

        mockMvc.perform(get("/admin/services/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/service-edit"))
                .andExpect(content().string(containsString("Document B&amp;W (A4)")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateService_valid_savesAndRedirects() throws Exception {
        mockMvc.perform(post("/admin/services/1/edit").with(csrf())
                        .param("name", "Document B&W (A4)")
                        .param("basePrice", "2.00")
                        .param("pricingType", "BLACK_WHITE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"));

        verify(commandService).updatePrintService(eq(1L), eq("Document B&W (A4)"), isNull(),
                eq(new BigDecimal("2.00")), eq(PricingType.BLACK_WHITE), isNull());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateService_reactivates() throws Exception {
        mockMvc.perform(post("/admin/services/1/activate").with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(commandService).updatePrintService(1L, null, null, null, null, true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateAddon_reactivates() throws Exception {
        mockMvc.perform(post("/admin/services/addons/2/activate").with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(commandService).updateAddonService(2L, null, null, null, true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePromotion_endBeforeStart_showsError() throws Exception {
        mockMvc.perform(post("/admin/promotions/10/edit").with(csrf())
                        .param("code", "SAVE30")
                        .param("discountType", "FIXED_AMOUNT")
                        .param("discountValue", "30")
                        .param("minOrderAmount", "200")
                        .param("startDate", "2026-12-31T00:00")
                        .param("endDate", "2026-01-01T00:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/promotion-edit"))
                .andExpect(model().attributeHasFieldErrors("form", "endDate"));

        verify(promotionService, never()).update(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editPromotionForm_codeIsReadOnly() throws Exception {
        Promotion promo = new Promotion();
        promo.setId(10L);
        promo.setCode("SAVE30");
        promo.setDiscountType(DiscountType.FIXED_AMOUNT);
        promo.setDiscountValue(new BigDecimal("30"));
        promo.setMinOrderAmount(new BigDecimal("200"));
        promo.setStartDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        promo.setEndDate(LocalDateTime.of(2026, 12, 31, 0, 0));
        when(promotionService.findById(10L)).thenReturn(promo);

        mockMvc.perform(get("/admin/promotions/10/edit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("แก้ไขโปรโมชัน SAVE30")))
                .andExpect(content().string(containsString("type=\"hidden\"")));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void editPages_asStaff_return403() throws Exception {
        mockMvc.perform(get("/admin/services/1/edit")).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/promotions/1/edit")).andExpect(status().isForbidden());
    }
}
