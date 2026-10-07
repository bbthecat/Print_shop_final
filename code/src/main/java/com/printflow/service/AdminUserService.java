package com.printflow.service;

import com.printflow.domain.enums.Role;
import com.printflow.dto.request.AdminUserCreateRequest;
import com.printflow.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<UserResponse> getUsers(Role role, Pageable pageable);

    UserResponse getById(Long id);

    UserResponse create(AdminUserCreateRequest request);

    UserResponse changeRole(Long id, Role role);

    UserResponse changeActive(Long id, boolean active);
}
