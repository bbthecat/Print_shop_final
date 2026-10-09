package com.printflow.service.impl;

import com.printflow.domain.entity.Payment;
import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.domain.enums.PaymentMethod;
import com.printflow.domain.enums.PaymentStatus;
import com.printflow.exception.InvalidStateTransitionException;
import com.printflow.repository.OrderRepository;
import com.printflow.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PrintOrder order(OrderStatus status) {
        return new PrintOrder("ORD-1", 1L, status, new BigDecimal("32.00"));
    }

    @Test
    void createUnpaidIfAbsent_noPayment_createsOneFromOrderTotal() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order(OrderStatus.PENDING)));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment payment = paymentService.createUnpaidIfAbsent(1L);

        assertEquals(new BigDecimal("32.00"), payment.getAmount());
        assertEquals(PaymentStatus.UNPAID, payment.getPaymentStatus());
    }

    @Test
    void createUnpaidIfAbsent_paymentExists_returnsExistingWithoutThrowing() {
        Payment existing = new Payment(1L, new BigDecimal("32.00"));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(existing));

        assertSame(existing, paymentService.createUnpaidIfAbsent(1L));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void markAsPaid_cancelledOrder_throwsAndDoesNotSave() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order(OrderStatus.CANCELLED)));

        assertThrows(InvalidStateTransitionException.class,
                () -> paymentService.markAsPaid(1L, PaymentMethod.CASH));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void markAsPaid_activeOrder_setsPaid() {
        Payment payment = new Payment(1L, new BigDecimal("32.00"));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order(OrderStatus.READY)));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        Payment result = paymentService.markAsPaid(1L, PaymentMethod.QR);

        assertEquals(PaymentStatus.PAID, result.getPaymentStatus());
        assertEquals(PaymentMethod.QR, result.getPaymentMethod());
    }
}
