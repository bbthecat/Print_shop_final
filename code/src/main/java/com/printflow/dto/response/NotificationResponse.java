package com.printflow.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(

        Long id,

        Long orderId,

        String title,

        String message,

        boolean read,

        LocalDateTime createdAt

) {
}