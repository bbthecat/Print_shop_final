package com.printflow.service.impl;

import com.printflow.domain.entity.Payment;
import com.printflow.domain.enums.PaymentMethod;
import com.printflow.domain.enums.PaymentStatus;
import com.printflow.exception.DuplicateResourceException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.repository.PaymentRepository;
import com.printflow.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment createUnpaid(Long orderId, BigDecimal amount) {
        paymentRepository.findByOrderId(orderId).ifPresent(existing -> {
            throw new DuplicateResourceException("Payment already exists for order: " + orderId);
        });

        return paymentRepository.save(new Payment(orderId, amount));
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
        Payment payment = findByOrderId(orderId);
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
}