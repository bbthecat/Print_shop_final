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
import com.printflow.service.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserProvider currentUserProvider;

    public AdminUserServiceImpl(UserRepository userRepository,
                                UserMapper userMapper,
                                PasswordEncoder passwordEncoder,
                                CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public Page<UserResponse> getUsers(Role role, Pageable pageable) {
        Page<User> users = role == null
                ? userRepository.findAll(pageable)
                : userRepository.findAllByRole(role, pageable);
        return users.map(userMapper::toResponse);
    }

    @Override
    public UserResponse getById(Long id) {
        return userMapper.toResponse(findUser(id));
    }

    @Override
    @Transactional
    public UserResponse create(AdminUserCreateRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }
        User user = userMapper.toEntity(request, passwordEncoder.encode(request.password()));
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse changeRole(Long id, Role role) {
        User user = findUser(id);
        rejectSelfChange(id, "change your own role");
        user.setRole(role);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse changeActive(Long id, boolean active) {
        User user = findUser(id);
        rejectSelfChange(id, "change your own active status");
        user.setActive(active);
        return userMapper.toResponse(user);
    }

    private void rejectSelfChange(Long targetId, String action) {
        if (targetId.equals(currentUserProvider.getCurrentUserId())) {
            throw new ValidationException("You cannot " + action);
        }
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
