package com.printflow.controller.web.admin;

import com.printflow.config.SecurityConfig;
import com.printflow.domain.enums.PricingType;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminServicePageController.class)
@Import(SecurityConfig.class)
class AdminServicePageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceCatalogQueryService queryService;

    @MockBean
    private ServiceCatalogCommandService commandService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_whenAdmin_shouldReturnView() throws Exception {
        when(queryService.findAllActivePrintServices()).thenReturn(List.of());
        when(queryService.findAllActiveAddonServices()).thenReturn(List.of());

        mockMvc.perform(get("/admin/services"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/services"))
                .andExpect(model().attributeExists("services", "addons", "serviceForm", "addonForm"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createService_whenValid_shouldRedirect() throws Exception {
        mockMvc.perform(post("/admin/services")
                        .with(csrf())
                        .param("name", "Doc Color")
                        .param("description", "High quality")
                        .param("basePrice", "5.00")
                        .param("pricingType", "COLOR"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"))
                .andExpect(flash().attributeExists("success"));

        verify(commandService).createPrintService(eq("Doc Color"), eq("High quality"), eq(new BigDecimal("5.00")), eq(PricingType.COLOR));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAddon_whenValid_shouldRedirect() throws Exception {
        mockMvc.perform(post("/admin/services/addons")
                        .with(csrf())
                        .param("name", "Staple")
                        .param("description", "Corner staple")
                        .param("price", "2.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"))
                .andExpect(flash().attributeExists("success"));

        verify(commandService).createAddonService(eq("Staple"), eq("Corner staple"), eq(new BigDecimal("2.00")));
    }

    @Test
    void list_whenAnonymous_shouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/admin/services"))
                .andExpect(status().isUnauthorized());
    }
}
