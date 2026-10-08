package com.printflow.service.impl;

import com.printflow.domain.entity.AddonService;
import com.printflow.domain.entity.PrintItem;
import com.printflow.domain.entity.PrintService;
import com.printflow.domain.entity.Promotion;
import com.printflow.domain.enums.DiscountType;
import com.printflow.domain.enums.PricingType;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.request.OrderCreateRequest;
import com.printflow.dto.request.OrderItemRequest;
import com.printflow.dto.response.OrderResponse;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.exception.ValidationException;
import com.printflow.mapper.OrderMapper;
import com.printflow.repository.OrderPromotionRepository;
import com.printflow.repository.OrderRepository;
import com.printflow.repository.PrintItemAddonRepository;
import com.printflow.repository.PrintItemRepository;
import com.printflow.service.PromotionService;
import com.printflow.service.ServiceCatalogQueryService;
import com.printflow.service.strategy.pricing.PricingCalculator;
import com.printflow.validation.OrderValidationContext;
import com.printflow.validation.OrderValidationHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PrintItemRepository printItemRepository;

    @Mock
    private PrintItemAddonRepository printItemAddonRepository;

    @Mock
    private OrderPromotionRepository orderPromotionRepository;

    @Mock
    private ServiceCatalogQueryService serviceCatalogQueryService;

    @Mock
    private PromotionService promotionService;

    @Mock
    private PricingCalculator pricingCalculator;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderValidationHandler orderValidationChain;

    @InjectMocks
    private OrderCommandServiceImpl orderCommandService;

    @Test
    void shouldCreateOrderSuccessfully() {
        PrintService printService = mock(PrintService.class);
        PrintItem savedItem = mock(PrintItem.class);
        OrderResponse response = mock(OrderResponse.class);

        when(printService.getId()).thenReturn(1L);
        when(printService.getPricingType()).thenReturn(PricingType.BLACK_WHITE);
        when(printService.getBasePrice()).thenReturn(BigDecimal.TEN);

        when(serviceCatalogQueryService.findActivePrintServiceById(1L))
                .thenReturn(printService);

        when(pricingCalculator.calculateItemTotal(
                any(),
                any(),
                anyInt(),
                anyInt(),
                any()
        )).thenReturn(BigDecimal.valueOf(20));

        when(orderRepository.save(any()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(savedItem.getId()).thenReturn(1L);

        when(printItemRepository.save(any(PrintItem.class)))
                .thenReturn(savedItem);

        when(printItemAddonRepository.findByItemId(1L))
                .thenReturn(List.of());

        when(orderMapper.toResponse(any(), any(), any()))
                .thenReturn(response);

        OrderItemRequest itemRequest =
                new OrderItemRequest(
                        1L,
                        2,
                        List.of()
                );

        OrderCreateRequest request =
                new OrderCreateRequest(
                        100L,
                        List.of(itemRequest),
                        null
                );

        OrderResponse result =
                orderCommandService.createOrder(request);

        assertEquals(response, result);

        verify(orderValidationChain)
                .handle(any(OrderValidationContext.class));

        verify(orderRepository, times(2))
                .save(any());

        verify(printItemRepository)
                .save(any(PrintItem.class));

        verify(pricingCalculator)
                .calculateItemTotal(
                        any(),
                        any(),
                        anyInt(),
                        anyInt(),
                        any()
                );

        verify(orderMapper)
                .toResponse(any(), any(), any());
    }

    @Test
    void shouldCreateOrderWithPromotion() {
        PrintService printService = mock(PrintService.class);
        PrintItem savedItem = mock(PrintItem.class);
        Promotion promotion = mock(Promotion.class);
        OrderResponse response = mock(OrderResponse.class);

        when(printService.getId()).thenReturn(1L);
        when(printService.getPricingType()).thenReturn(PricingType.BLACK_WHITE);
        when(printService.getBasePrice()).thenReturn(BigDecimal.TEN);

        when(serviceCatalogQueryService.findActivePrintServiceById(1L))
                .thenReturn(printService);

        when(pricingCalculator.calculateItemTotal(
                any(),
                any(),
                anyInt(),
                anyInt(),
                any()
        )).thenReturn(BigDecimal.valueOf(100));

        when(orderRepository.save(any()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(savedItem.getId()).thenReturn(1L);

        when(printItemRepository.save(any(PrintItem.class)))
                .thenReturn(savedItem);

        when(printItemAddonRepository.findByItemId(1L))
                .thenReturn(List.of());

        when(promotionService.findValidByCode("SAVE10"))
                .thenReturn(promotion);

        when(promotion.getId()).thenReturn(10L);
        when(promotion.getDiscountType())
                .thenReturn(DiscountType.PERCENTAGE);
        when(promotion.getDiscountValue())
                .thenReturn(BigDecimal.TEN);
        when(promotion.getMinOrderAmount())
                .thenReturn(BigDecimal.ZERO);

        when(orderMapper.toResponse(any(), any(), any()))
                .thenReturn(response);

        OrderItemRequest itemRequest =
                new OrderItemRequest(
                        1L,
                        2,
                        List.of()
                );

        OrderCreateRequest request =
                new OrderCreateRequest(
                        100L,
                        List.of(itemRequest),
                        "SAVE10"
                );

        OrderResponse result =
                orderCommandService.createOrder(request);

        assertEquals(response, result);

        verify(orderValidationChain)
                .handle(any(OrderValidationContext.class));

        verify(promotionService, atLeastOnce())
                .findValidByCode("SAVE10");

        verify(orderPromotionRepository)
                .save(any());

        verify(orderRepository, times(2))
                .save(any());

        verify(orderMapper)
                .toResponse(any(), any(), any());
    }

    @Test
    void shouldThrowWhenValidationFails() {
        OrderItemRequest itemRequest =
                new OrderItemRequest(1L, 0, List.of());
        OrderCreateRequest request =
                new OrderCreateRequest(100L, List.of(itemRequest), null);

        doThrow(new ValidationException("Quantity must be greater than 0"))
                .when(orderValidationChain)
                .handle(any(OrderValidationContext.class));

        assertThrows(
                ValidationException.class,
                () -> orderCommandService.createOrder(request)
        );

        verify(orderValidationChain)
                .handle(any(OrderValidationContext.class));
        verify(orderRepository, never())
                .save(any());
        verify(printItemRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenDeletingNonExistingOrder() {
        when(orderRepository.existsById(999L))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderCommandService.deleteOrder(999L)
        );

        verify(orderRepository)
                .existsById(999L);

        verify(orderRepository, never())
                .deleteById(any());
    }

    @Test
    void shouldDeleteExistingOrder() {
        when(orderRepository.existsById(1L))
                .thenReturn(true);

        assertDoesNotThrow(
                () -> orderCommandService.deleteOrder(1L)
        );

        verify(orderRepository)
                .existsById(1L);

        verify(orderRepository)
                .deleteById(1L);
    }
}