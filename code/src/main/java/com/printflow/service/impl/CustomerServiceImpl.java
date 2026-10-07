package com.printflow.service.impl;

import com.printflow.domain.entity.User;
import com.printflow.domain.enums.Role;
import com.printflow.dto.request.CustomerRegisterRequest;
import com.printflow.dto.request.CustomerUpdateRequest;
import com.printflow.dto.response.CustomerResponse;
import com.printflow.exception.DuplicateResourceException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.mapper.CustomerMapper;
import com.printflow.repository.UserRepository;
import com.printflow.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    public CustomerServiceImpl(UserRepository userRepository,
                               CustomerMapper customerMapper,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerMapper = customerMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public CustomerResponse register(CustomerRegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }
        User user = customerMapper.toEntity(request, passwordEncoder.encode(request.password()));
        return customerMapper.toResponse(userRepository.save(user));
    }

    @Override
    public CustomerResponse getById(Long id) {
        return customerMapper.toResponse(findCustomer(id));
    }

    @Override
    public Page<CustomerResponse> getAll(Pageable pageable) {
        return userRepository.findAllByRole(Role.CUSTOMER, pageable)
                .map(customerMapper::toResponse);
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerUpdateRequest request) {
        User user = findCustomer(id);
        boolean emailChanged = !user.getEmail().equalsIgnoreCase(request.email());
        if (emailChanged && userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }
        customerMapper.updateEntity(user, request);
        return customerMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        User user = findCustomer(id);
        user.setActive(false);
    }

    private User findCustomer(Long id) {
        return userRepository.findByIdAndRole(id, Role.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }
}
