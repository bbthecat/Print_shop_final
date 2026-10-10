package com.printflow.service.listener;

import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.entity.User;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.domain.enums.Role;
import com.printflow.repository.OrderRepository;
import com.printflow.repository.UserRepository;
import com.printflow.service.NotificationService;
import com.printflow.service.PaymentService;
import com.printflow.service.event.OrderCreatedEvent;
import com.printflow.service.event.OrderStatusChangedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewObserverListenersTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("order ถูกยกเลิก → คืนเงินถ้าจ่ายแล้ว")
    void refundListener_onCancel_refunds() {
        new PaymentRefundListener(paymentService)
                .onStatusChanged(new OrderStatusChangedEvent(1L, OrderStatus.CONFIRMED, OrderStatus.CANCELLED, 9L));

        verify(paymentService).refundIfPaid(1L);
    }

    @Test
    @DisplayName("เปลี่ยนสถานะอื่น → ไม่ยุ่งกับ payment")
    void refundListener_onOtherStatus_doesNothing() {
        new PaymentRefundListener(paymentService)
                .onStatusChanged(new OrderStatusChangedEvent(1L, OrderStatus.PENDING, OrderStatus.CONFIRMED, 9L));

        verify(paymentService, never()).refundIfPaid(any());
    }

    @Test
    @DisplayName("มี order ใหม่ → แจ้งเตือน STAFF ทุกคน")
    void staffListener_notifiesEveryActiveStaff() {
        User staff1 = mock(User.class);
        User staff2 = mock(User.class);
        when(staff1.getId()).thenReturn(21L);
        when(staff2.getId()).thenReturn(22L);
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(new PrintOrder("ORD-1", 7L, OrderStatus.PENDING, new BigDecimal("32.00"))));
        when(userRepository.findAllByRoleAndActiveTrue(Role.STAFF)).thenReturn(List.of(staff1, staff2));

        new StaffNewOrderListener(orderRepository, userRepository, notificationService)
                .onOrderCreated(new OrderCreatedEvent(1L));

        verify(notificationService).create(eq(21L), any(), eq("มีคำสั่งพิมพ์ใหม่"), anyString());
        verify(notificationService).create(eq(22L), any(), eq("มีคำสั่งพิมพ์ใหม่"), anyString());
        verify(notificationService, times(2)).create(any(), any(), anyString(), anyString());
    }
}
