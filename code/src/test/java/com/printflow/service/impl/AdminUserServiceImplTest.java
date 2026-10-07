package com.printflow.service.impl;

import com.printflow.domain.entity.User;
import com.printflow.domain.enums.Role;
import com.printflow.dto.request.AdminUserCreateRequest;
import com.printflow.dto.response.UserResponse;
import com.printflow.exception.DuplicateResourceException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.exception.ValidationException;
import com.printflow.mapper.UserMapper;
import com.printflow.repository.UserRepository;
import com.printflow.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    private static final Long ADMIN_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private AdminUserServiceImpl adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserServiceImpl(userRepository, new UserMapper(), passwordEncoder, currentUserProvider);
    }

    private AdminUserCreateRequest staffRequest() {
        return new AdminUserCreateRequest("staff01", "staff01@example.com", "password123",
                "Somsak", "Staff", null, Role.STAFF);
    }

    private User existingUser(Long id, Role role) {
        User user = new UserMapper().toEntity(staffRequest(), "hashed");
        user.setId(id);
        user.setRole(role);
        return user;
    }

    @Test
    void create_success_savesUserWithRequestedRoleAndHashedPassword() {
        when(userRepository.existsByUsername("staff01")).thenReturn(false);
        when(userRepository.existsByEmail("staff01@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = adminUserService.create(staffRequest());

        assertThat(response.role()).isEqualTo(Role.STAFF);
        assertThat(response.username()).isEqualTo("staff01");
    }

    @Test
    void create_duplicateUsername_throwsDuplicateResourceException() {
        when(userRepository.existsByUsername("staff01")).thenReturn(true);

        assertThatThrownBy(() -> adminUserService.create(staffRequest()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUsers_withoutRole_returnsAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(existingUser(2L, Role.STAFF), existingUser(3L, Role.CUSTOMER))));

        Page<UserResponse> page = adminUserService.getUsers(null, pageable);

        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void getUsers_withRole_filtersByRole() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAllByRole(Role.STAFF, pageable))
                .thenReturn(new PageImpl<>(List.of(existingUser(2L, Role.STAFF))));

        Page<UserResponse> page = adminUserService.getUsers(Role.STAFF, pageable);

        assertThat(page.getContent()).extracting(UserResponse::role).containsOnly(Role.STAFF);
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void changeRole_otherUser_updatesRole() {
        User user = existingUser(2L, Role.CUSTOMER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(currentUserProvider.getCurrentUserId()).thenReturn(ADMIN_ID);

        UserResponse response = adminUserService.changeRole(2L, Role.STAFF);

        assertThat(response.role()).isEqualTo(Role.STAFF);
    }

    @Test
    void changeRole_self_throwsValidationException() {
        User admin = existingUser(ADMIN_ID, Role.ADMIN);
        when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
        when(currentUserProvider.getCurrentUserId()).thenReturn(ADMIN_ID);

        assertThatThrownBy(() -> adminUserService.changeRole(ADMIN_ID, Role.CUSTOMER))
                .isInstanceOf(ValidationException.class);
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void changeActive_self_throwsValidationException() {
        User admin = existingUser(ADMIN_ID, Role.ADMIN);
        when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
        when(currentUserProvider.getCurrentUserId()).thenReturn(ADMIN_ID);

        assertThatThrownBy(() -> adminUserService.changeActive(ADMIN_ID, false))
                .isInstanceOf(ValidationException.class);
        assertThat(admin.isActive()).isTrue();
    }

    @Test
    void changeActive_otherUser_deactivates() {
        User user = existingUser(2L, Role.STAFF);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(currentUserProvider.getCurrentUserId()).thenReturn(ADMIN_ID);

        adminUserService.changeActive(2L, false);

        assertThat(user.isActive()).isFalse();
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
