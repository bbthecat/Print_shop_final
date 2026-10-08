package com.printflow.service.listener;

import com.printflow.domain.entity.OrderStatusHistory;
import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.repository.OrderRepository;
import com.printflow.repository.OrderStatusHistoryRepository;
import com.printflow.service.NotificationService;
import com.printflow.service.event.OrderStatusChangedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderStatusListenerTest {

    @Mock
    private OrderStatusHistoryRepository historyRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderHistoryListener historyListener;

    @InjectMocks
    private InAppNotificationListener notificationListener;

    @Captor
    private ArgumentCaptor<OrderStatusHistory> historyCaptor;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private OrderStatusChangedEvent event(OrderStatus from, OrderStatus to) {
        return new OrderStatusChangedEvent(1L, from, to, 9L);
    }

    @Test
    @DisplayName("เปลี่ยนสถานะแล้วบันทึกประวัติครบทุกช่อง")
    void historyIsRecorded() {
        historyListener.onStatusChanged(event(OrderStatus.PENDING, OrderStatus.CONFIRMED));

        verify(historyRepository).save(historyCaptor.capture());
        OrderStatusHistory history = historyCaptor.getValue();

        assertEquals(1L, history.getOrderId());
        assertEquals(OrderStatus.PENDING, history.getOldStatus());
        assertEquals(OrderStatus.CONFIRMED, history.getNewStatus());
        assertEquals(9L, history.getChangedBy());
    }

    @Test
    @DisplayName("แจ้งเตือนต้องส่งถึงเจ้าของคำสั่งซื้อพร้อมเลขที่ออเดอร์")
    void notificationGoesToOrderOwner() {
        PrintOrder order = new PrintOrder("ORD-001", 7L, OrderStatus.READY, new BigDecimal("100.00"));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        notificationListener.onStatusChanged(event(OrderStatus.PROCESSING, OrderStatus.READY));

        verify(notificationService).create(
                eq(7L),
                eq(1L),
                eq("งานเสร็จแล้ว"),
                messageCaptor.capture());

        assertTrue(messageCaptor.getValue().contains("ORD-001"));
    }

    @Test
    @DisplayName("สถานะ PENDING ไม่สร้างการแจ้งเตือน")
    void pendingStatusCreatesNoNotification() {
        notificationListener.onStatusChanged(event(OrderStatus.PENDING, OrderStatus.PENDING));

        verify(notificationService, never()).create(any(), any(), any(), any());
        verify(orderRepository, never()).findById(any());
    }

    @Test
    @DisplayName("ไม่พบคำสั่งซื้อต้องไม่พังและไม่สร้างแจ้งเตือน")
    void missingOrderIsIgnored() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        notificationListener.onStatusChanged(event(OrderStatus.PENDING, OrderStatus.CONFIRMED));

        verify(notificationService, never()).create(any(), any(), any(), any());
    }
}