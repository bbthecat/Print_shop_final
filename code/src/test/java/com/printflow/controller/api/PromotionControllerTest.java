package com.printflow.controller.api;

import com.printflow.config.SecurityConfig;
import com.printflow.domain.entity.Promotion;
import com.printflow.domain.enums.DiscountType;
import com.printflow.dto.response.PromotionResponse;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.mapper.PromotionMapper;
import com.printflow.service.PromotionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(PromotionController.class)
@Import(SecurityConfig.class)
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionService promotionService;

    @MockBean
    private PromotionMapper mapper;

    @Test
    @WithMockUser
    void getAll_shouldReturnActivePromotions() throws Exception {
        Promotion promo = new Promotion();
        promo.setId(1L);
        promo.setCode("WELCOME10");
        PromotionResponse response = new PromotionResponse(
                1L, "WELCOME10", "10% off", DiscountType.PERCENTAGE, new BigDecimal("10.00"),
                BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now().plusDays(10), true,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(promotionService.findAllActive()).thenReturn(List.of(promo));
        when(mapper.toResponse(promo)).thenReturn(response);

        mockMvc.perform(get("/api/v1/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("WELCOME10"));
    }

    @Test
    @WithMockUser
    void validateCode_whenValid_shouldReturn200() throws Exception {
        Promotion promo = new Promotion();
        promo.setId(1L);
        promo.setCode("SAVE30");
        PromotionResponse response = new PromotionResponse(
                1L, "SAVE30", "30 THB off", DiscountType.FIXED_AMOUNT, new BigDecimal("30.00"),
                BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now().plusDays(10), true,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(promotionService.findValidByCode("SAVE30")).thenReturn(promo);
        when(mapper.toResponse(promo)).thenReturn(response);

        mockMvc.perform(get("/api/v1/promotions/validate/SAVE30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SAVE30"))
                .andExpect(jsonPath("$.discountType").value("FIXED_AMOUNT"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(promotionService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Promotion not found"));

        mockMvc.perform(get("/api/v1/promotions/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_whenValid_shouldReturn201() throws Exception {
        Promotion promo = new Promotion();
        promo.setId(1L);
        promo.setCode("NEW50");
        PromotionResponse response = new PromotionResponse(
                1L, "NEW50", "Desc", DiscountType.FIXED_AMOUNT, new BigDecimal("50.00"),
                BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now().plusDays(5), true,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(promotionService.create(eq("NEW50"), any(), eq(DiscountType.FIXED_AMOUNT), any(), any(), any(), any()))
                .thenReturn(promo);
        when(mapper.toResponse(promo)).thenReturn(response);

        String json = """
                {
                    "code": "NEW50",
                    "description": "Desc",
                    "discountType": "FIXED_AMOUNT",
                    "discountValue": 50.00,
                    "minOrderAmount": 0.00,
                    "startDate": "2026-01-01T00:00:00",
                    "endDate": "2026-12-31T23:59:59"
                }
                """;

        mockMvc.perform(post("/api/v1/promotions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.code").value("NEW50"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_whenPercentageExceeds100_shouldReturn400() throws Exception {
        String json = """
                {
                    "code": "OVER100",
                    "description": "Too much discount",
                    "discountType": "PERCENTAGE",
                    "discountValue": 150.00,
                    "minOrderAmount": 0.00,
                    "startDate": "2026-01-01T00:00:00",
                    "endDate": "2026-12-31T23:59:59"
                }
                """;

        mockMvc.perform(post("/api/v1/promotions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/promotions/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(promotionService).deactivate(1L);
    }
}
