package com.printflow.service;

import com.printflow.domain.entity.Payment;
import com.printflow.domain.enums.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentService {

    Payment createUnpaid(Long orderId, BigDecimal amount);

    Payment findByOrderId(Long orderId);

    Payment markAsPaid(Long orderId, PaymentMethod method);
}