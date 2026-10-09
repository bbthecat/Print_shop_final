package com.printflow.service;

import com.printflow.domain.entity.Payment;
import com.printflow.domain.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    Payment createUnpaid(Long orderId);

    // สร้าง payment UNPAID ถ้ายังไม่มี ถ้ามีแล้วคืนตัวเดิม (ไม่ throw)
    Payment createUnpaidIfAbsent(Long orderId);

    Payment findByOrderId(Long orderId);

    Payment markAsPaid(Long orderId, PaymentMethod method);

    Page<Payment> findAll(Pageable pageable);
}