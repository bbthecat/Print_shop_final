package com.printflow.dto.response;

import com.printflow.domain.enums.PaymentMethod;
import com.printflow.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(

        Long id,

        Long orderId,

        BigDecimal amount,

        PaymentMethod paymentMethod,

        PaymentStatus paymentStatus,

        LocalDateTime paidAt

) {
}