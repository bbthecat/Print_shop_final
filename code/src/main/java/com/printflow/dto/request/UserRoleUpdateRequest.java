package com.printflow.dto.request;

import com.printflow.domain.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(@NotNull Role role) {
}
