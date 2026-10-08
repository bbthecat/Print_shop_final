package com.printflow.service.impl;

import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.exception.InvalidStateTransitionException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.repository.OrderRepository;
import com.printflow.service.event.OrderStatusChangedEvent;
import com.printflow.service.state.OrderStateResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderStatusServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<OrderStatusChangedEvent> eventCaptor;

    private OrderStatusServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderStatusServiceImpl(
                orderRepository,
                new OrderStateResolver(),
                eventPublisher
        );
    }

    private PrintOrder orderWith(OrderStatus status) {
        return new PrintOrder("ORD-001", 7L, status, new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("ยืนยันคำสั่งซื้อที่ PENDING แล้วสถานะเปลี่ยนเป็น CONFIRMED")
    void confirmPendingOrder() {
        PrintOrder order = orderWith(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderStatus result = service.changeStatus(1L, OrderStatus.CONFIRMED, 9L);

        assertEquals(OrderStatus.CONFIRMED, result);
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("เปลี่ยนสถานะสำเร็จแล้วต้อง publish event พร้อมข้อมูลครบ")
    void publishesEventAfterChange() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderWith(OrderStatus.PENDING)));

        service.changeStatus(1L, OrderStatus.CONFIRMED, 9L);

        verify(eventPublisher).publishEvent(eventCaptor.capture());
        OrderStatusChangedEvent event = eventCaptor.getValue();

        assertEquals(1L, event.orderId());
        assertEquals(OrderStatus.PENDING, event.oldStatus());
        assertEquals(OrderStatus.CONFIRMED, event.newStatus());
        assertEquals(9L, event.changedBy());
    }

    @Test
    @DisplayName("เปลี่ยนสถานะผิดลำดับต้องไม่บันทึกและไม่ publish event")
    void invalidTransitionIsRejected() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderWith(OrderStatus.COMPLETED)));

        assertThrows(
                InvalidStateTransitionException.class,
                () -> service.changeStatus(1L, OrderStatus.PROCESSING, 9L));

        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(OrderStatusChangedEvent.class));
    }

    @Test
    @DisplayName("ไม่พบคำสั่งซื้อต้องโยน ResourceNotFoundException")
    void orderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.changeStatus(99L, OrderStatus.CONFIRMED, 9L));
    }
}