package com.printflow.service.listener;

import com.printflow.service.PaymentService;
import com.printflow.service.event.OrderCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentCreationListenerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentCreationListener listener;

    @Test
    @DisplayName("สร้าง order แล้วต้องเกิด payment แบบ UNPAID (ถ้ายังไม่มี)")
    void createsPaymentOnOrderCreated() {
        listener.onOrderCreated(new OrderCreatedEvent(1L));

        verify(paymentService).createUnpaidIfAbsent(1L);
    }
}
