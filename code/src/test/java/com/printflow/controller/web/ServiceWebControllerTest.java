package com.printflow.controller.web;

import com.printflow.config.SecurityConfig;
import com.printflow.service.PromotionService;
import com.printflow.service.ServiceCatalogQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceWebController.class)
@Import(SecurityConfig.class)
class ServiceWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceCatalogQueryService catalogQueryService;

    @MockBean
    private PromotionService promotionService;

    @Test
    void listServices_shouldReturnServicesViewAndModel() throws Exception {
        when(catalogQueryService.findAllActivePrintServices()).thenReturn(List.of());
        when(catalogQueryService.findAllActiveAddonServices()).thenReturn(List.of());
        when(promotionService.findAllActive()).thenReturn(List.of());

        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(view().name("services/index"))
                .andExpect(model().attributeExists("services", "addons", "promotions"));
    }
}
