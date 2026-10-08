package com.printflow.service;

import com.printflow.domain.entity.Payment;
import com.printflow.domain.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface PaymentService {

    Payment createUnpaid(Long orderId, BigDecimal amount);

    Payment findByOrderId(Long orderId);

    Payment markAsPaid(Long orderId, PaymentMethod method);

    Page<Payment> findAll(Pageable pageable);
}