package com.printflow.mapper;

import com.printflow.domain.entity.User;
import com.printflow.domain.entity.UserProfile;
import com.printflow.domain.enums.Role;
import com.printflow.dto.request.CustomerRegisterRequest;
import com.printflow.dto.request.CustomerUpdateRequest;
import com.printflow.dto.response.CustomerResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public User toEntity(CustomerRegisterRequest request, String passwordHash) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordHash);
        user.setRole(Role.CUSTOMER);

        UserProfile profile = new UserProfile();
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhoneNumber(request.phoneNumber());
        profile.setAddress(request.address());
        profile.setUser(user);
        user.setProfile(profile);
        return user;
    }

    public void updateEntity(User user, CustomerUpdateRequest request) {
        user.setEmail(request.email());
        UserProfile profile = user.getProfile();
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhoneNumber(request.phoneNumber());
        profile.setAddress(request.address());
    }

    public CustomerResponse toResponse(User user) {
        UserProfile profile = user.getProfile();
        return new CustomerResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                profile != null ? profile.getFirstName() : null,
                profile != null ? profile.getLastName() : null,
                profile != null ? profile.getPhoneNumber() : null,
                profile != null ? profile.getAddress() : null,
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
