package com.printflow.service.impl;

import com.printflow.domain.entity.Payment;
import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.enums.PaymentMethod;
import com.printflow.domain.enums.PaymentStatus;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.exception.DuplicateResourceException;
import com.printflow.exception.InvalidStateTransitionException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.repository.OrderRepository;
import com.printflow.repository.PaymentRepository;
import com.printflow.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public Payment createUnpaid(Long orderId) {
        PrintOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        paymentRepository.findByOrderId(orderId).ifPresent(existing -> {
            throw new DuplicateResourceException("Payment already exists for order: " + orderId);
        });

        return paymentRepository.save(new Payment(orderId, order.getTotalPrice()));
    }

    // เช็กก่อนสร้าง แทนการ catch DuplicateResourceException
    // (ถ้า exception หลุดออกจาก method @Transactional จะทำให้ transaction ทั้งก้อนถูก rollback)
    @Override
    public Payment createUnpaidIfAbsent(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseGet(() -> createUnpaid(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for order: " + orderId));
    }

    @Override
    public Payment markAsPaid(Long orderId, PaymentMethod method) {
        PrintOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        // งานที่ยกเลิกแล้วรับชำระไม่ได้ ไม่งั้นรายงานยอดขายจะนับเงินของงานที่ยกเลิก
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidStateTransitionException("Cannot pay a cancelled order: " + orderId);
        }

        Payment payment = findByOrderId(orderId);
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Payment> findAll(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }
}