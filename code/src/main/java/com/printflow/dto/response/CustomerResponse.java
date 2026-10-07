package com.printflow.dto.response;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String address,
        boolean active,
        LocalDateTime createdAt
) {
}
