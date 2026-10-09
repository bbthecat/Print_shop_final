package com.printflow.config;

import com.printflow.controller.api.AddonServiceController;
import com.printflow.controller.api.PrintServiceController;
import com.printflow.controller.api.PromotionController;
import com.printflow.mapper.PromotionMapper;
import com.printflow.mapper.ServiceCatalogMapper;
import com.printflow.service.PromotionService;
import com.printflow.service.ServiceCatalogCommandService;
import com.printflow.service.ServiceCatalogQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks the SecurityConfig rules for the service catalog and promotions:
 * anyone can browse services, only ADMIN can change them.
 */
@WebMvcTest({PrintServiceController.class, AddonServiceController.class, PromotionController.class})
@Import(SecurityConfig.class)
class CatalogSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceCatalogQueryService queryService;

    @MockBean
    private ServiceCatalogCommandService commandService;

    @MockBean
    private ServiceCatalogMapper serviceCatalogMapper;

    @MockBean
    private PromotionService promotionService;

    @MockBean
    private PromotionMapper promotionMapper;

    @Test
    void browseServices_anonymous_isAllowed() throws Exception {
        when(queryService.findAllActivePrintServices(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isOk());
    }

    @Test
    void browseAddons_anonymous_isAllowed() throws Exception {
        when(queryService.findAllActiveAddonServices()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/addon-services"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteService_anonymous_returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/services/1"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(commandService);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void deleteService_asCustomer_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/services/1"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(commandService);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void createService_asStaff_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/services").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(commandService);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateAddon_asCustomer_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/addon-services/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(commandService);
    }

    @Test
    void browsePromotions_anonymous_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/promotions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createPromotion_asCustomer_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/promotions").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(promotionService);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void deletePromotion_asCustomer_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/promotions/1"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(promotionService);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getPromotionById_asCustomer_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/promotions/1"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(promotionService);
    }
}
