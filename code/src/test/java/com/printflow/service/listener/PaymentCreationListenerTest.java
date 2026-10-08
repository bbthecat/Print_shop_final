package com.printflow.service.listener;

import com.printflow.exception.DuplicateResourceException;
import com.printflow.service.PaymentService;
import com.printflow.service.event.OrderCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentCreationListenerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentCreationListener listener;

    @Test
    @DisplayName("สร้าง order แล้วต้องเกิด payment แบบ UNPAID")
    void createsPaymentOnOrderCreated() {
        listener.onOrderCreated(new OrderCreatedEvent(1L));

        verify(paymentService).createUnpaid(1L);
    }

    @Test
    @DisplayName("ถ้า payment มีอยู่แล้วต้องไม่พังและไม่สร้างซ้ำ")
    void ignoresDuplicatePayment() {
        doThrow(new DuplicateResourceException("exists"))
                .when(paymentService).createUnpaid(1L);

        assertDoesNotThrow(() -> listener.onOrderCreated(new OrderCreatedEvent(1L)));
    }
}