package com.printflow.dto.request;

import com.printflow.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PaymentUpdateRequest(

        @NotNull
        PaymentMethod paymentMethod

) {
}