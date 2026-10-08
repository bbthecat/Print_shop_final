package com.printflow.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.request.OrderCreateRequest;
import com.printflow.dto.request.OrderItemRequest;
import com.printflow.dto.response.OrderResponse;
import com.printflow.service.OrderCommandService;
import com.printflow.service.OrderQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@WithMockUser
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderCommandService commandService;

    @MockBean
    private OrderQueryService queryService;

    @Test
    void shouldGetAllOrders() throws Exception {
        OrderResponse response = createOrderResponse();

        when(queryService.getAll(any()))
                .thenReturn(new PageImpl<>(
                        List.of(response),
                        PageRequest.of(0, 10),
                        1
                ));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk());

        verify(queryService).getAll(any());
    }

    @Test
    void shouldGetOrderById() throws Exception {
        when(queryService.getById(1L))
                .thenReturn(createOrderResponse());

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk());

        verify(queryService).getById(1L);
    }

    @Test
    void shouldCreateOrder() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest(
                1L,
                List.of(new OrderItemRequest(
                        1L,
                        2,
                        List.of()
                )),
                null
        );

        when(commandService.createOrder(any(OrderCreateRequest.class)))
                .thenReturn(createOrderResponse());

        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(commandService).createOrder(any(OrderCreateRequest.class));
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        doNothing().when(commandService).deleteOrder(1L);

        mockMvc.perform(delete("/api/v1/orders/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(commandService).deleteOrder(1L);
    }

    private OrderResponse createOrderResponse() {
        return new OrderResponse(
                1L,
                "ORD-TEST-001",
                1L,
                OrderStatus.PENDING,
                BigDecimal.valueOf(100),
                LocalDateTime.now(),
                List.of()
        );
    }
}
