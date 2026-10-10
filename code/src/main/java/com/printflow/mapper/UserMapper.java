package com.printflow.mapper;

import com.printflow.domain.entity.User;
import com.printflow.domain.entity.UserProfile;
import com.printflow.dto.request.AdminUserCreateRequest;
import com.printflow.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(AdminUserCreateRequest request, String passwordHash) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordHash);
        user.setRole(request.role());

        UserProfile profile = new UserProfile();
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhoneNumber(request.phoneNumber());
        profile.setUser(user);
        user.setProfile(profile);
        return user;
    }

    public UserResponse toResponse(User user) {
        UserProfile profile = user.getProfile();
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                profile != null ? profile.getFirstName() : null,
                profile != null ? profile.getLastName() : null,
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
