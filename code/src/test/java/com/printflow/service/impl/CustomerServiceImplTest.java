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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class CustomerServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(userRepository, new CustomerMapper(), passwordEncoder);
    }

    private CustomerRegisterRequest registerRequest() {
        return new CustomerRegisterRequest("somchai", "somchai@example.com", "password123",
                "Somchai", "Jaidee", "0812345678", "Khon Kaen");
    }

    private User existingCustomer() {
        User user = new CustomerMapper().toEntity(registerRequest(), "hashed");
        user.setId(1L);
        return user;
    }

    @Test
    void register_success_hashesPasswordAndSavesCustomer() {
        when(userRepository.existsByUsername("somchai")).thenReturn(false);
        when(userRepository.existsByEmail("somchai@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        CustomerResponse response = customerService.register(registerRequest());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(saved.getProfile().getUser()).isSameAs(saved);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("somchai");
        assertThat(response.firstName()).isEqualTo("Somchai");
    }

    @Test
    void register_duplicateUsername_throwsDuplicateResourceException() {
        when(userRepository.existsByUsername("somchai")).thenReturn(true);

        assertThatThrownBy(() -> customerService.register(registerRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResourceException() {
        when(userRepository.existsByUsername("somchai")).thenReturn(false);
        when(userRepository.existsByEmail("somchai@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.register(registerRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email");
        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(userRepository.findByIdAndRole(99L, Role.CUSTOMER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAll_returnsOnlyCustomersAsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAllByRole(Role.CUSTOMER, pageable))
                .thenReturn(new PageImpl<>(List.of(existingCustomer()), pageable, 1));

        Page<CustomerResponse> page = customerService.getAll(pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).username()).isEqualTo("somchai");
    }

    @Test
    void update_success_changesEmailAndProfile() {
        User user = existingCustomer();
        when(userRepository.findByIdAndRole(1L, Role.CUSTOMER)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        CustomerResponse response = customerService.update(1L,
                new CustomerUpdateRequest("new@example.com", "Somsri", "Jaidee", null, null));

        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.firstName()).isEqualTo("Somsri");
    }

    @Test
    void update_emailTakenByOtherUser_throwsDuplicateResourceException() {
        when(userRepository.findByIdAndRole(1L, Role.CUSTOMER)).thenReturn(Optional.of(existingCustomer()));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.update(1L,
                new CustomerUpdateRequest("taken@example.com", "Somchai", "Jaidee", null, null)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void deactivate_setsActiveFalseInsteadOfDeleting() {
        User user = existingCustomer();
        when(userRepository.findByIdAndRole(1L, Role.CUSTOMER)).thenReturn(Optional.of(user));

        customerService.deactivate(1L);

        assertThat(user.isActive()).isFalse();
        verify(userRepository, never()).delete(any());
    }
}
