package com.printflow.dto.response;

import com.printflow.domain.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        Role role,
        boolean active,
        LocalDateTime createdAt
) {
}
