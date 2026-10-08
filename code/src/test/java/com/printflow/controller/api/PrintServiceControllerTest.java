package com.printflow.controller.api;

import com.printflow.config.SecurityConfig;
import com.printflow.domain.entity.PrintService;
import com.printflow.domain.enums.PricingType;
import com.printflow.dto.request.PrintServiceRequest;
import com.printflow.dto.response.PrintServiceResponse;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.mapper.ServiceCatalogMapper;
import com.printflow.service.ServiceCatalogCommandService;
import com.printflow.service.ServiceCatalogQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrintServiceController.class)
@Import(SecurityConfig.class)
class PrintServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceCatalogQueryService queryService;

    @MockBean
    private ServiceCatalogCommandService commandService;

    @MockBean
    private ServiceCatalogMapper mapper;

    @Test
    @WithMockUser
    void getAll_shouldReturnPagedServices() throws Exception {
        PrintService service = new PrintService();
        service.setId(1L);
        service.setName("Doc B&W");
        PrintServiceResponse response = new PrintServiceResponse(
                1L, "Doc B&W", "Desc", new BigDecimal("1.50"), PricingType.BLACK_WHITE, true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(queryService.findAllActivePrintServices(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(service)));
        when(mapper.toResponse(service)).thenReturn(response);

        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Doc B&W"));
    }

    @Test
    @WithMockUser
    void getById_whenFound_shouldReturn200() throws Exception {
        PrintService service = new PrintService();
        service.setId(1L);
        PrintServiceResponse response = new PrintServiceResponse(
                1L, "Doc B&W", "Desc", new BigDecimal("1.50"), PricingType.BLACK_WHITE, true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(queryService.findActivePrintServiceById(1L)).thenReturn(service);
        when(mapper.toResponse(service)).thenReturn(response);

        mockMvc.perform(get("/api/v1/services/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(queryService.findActivePrintServiceById(99L))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/services/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void create_whenValid_shouldReturn201() throws Exception {
        PrintService service = new PrintService();
        service.setId(1L);
        PrintServiceResponse response = new PrintServiceResponse(
                1L, "Doc Color", "Desc", new BigDecimal("5.00"), PricingType.COLOR, true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(commandService.createPrintService(eq("Doc Color"), eq("Desc"), eq(new BigDecimal("5.00")), eq(PricingType.COLOR)))
                .thenReturn(service);
        when(mapper.toResponse(service)).thenReturn(response);

        String json = """
                {
                    "name": "Doc Color",
                    "description": "Desc",
                    "basePrice": 5.00,
                    "pricingType": "COLOR"
                }
                """;

        mockMvc.perform(post("/api/v1/services")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Doc Color"));
    }

    @Test
    @WithMockUser
    void create_whenInvalid_shouldReturn400() throws Exception {
        // Missing name and basePrice
        String json = """
                {
                    "name": "",
                    "pricingType": "COLOR"
                }
                """;

        mockMvc.perform(post("/api/v1/services")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/services/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(commandService).deactivatePrintService(1L);
    }
}
