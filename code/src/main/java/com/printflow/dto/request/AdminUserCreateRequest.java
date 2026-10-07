package com.printflow.dto.request;

import com.printflow.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminUserCreateRequest(
        @NotBlank @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "must contain only letters, digits, '_' or '.'")
        String username,

        @NotBlank @Email @Size(max = 100)
        String email,

        @NotBlank @Size(min = 8, max = 100)
        String password,

        @NotBlank @Size(max = 50)
        String firstName,

        @NotBlank @Size(max = 50)
        String lastName,

        @Size(max = 20)
        String phoneNumber,

        @NotNull
        Role role
) {
}
