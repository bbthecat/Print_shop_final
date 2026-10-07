package com.printflow.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("customerAccess")
public class CustomerAccessChecker {

    private static final Set<String> STAFF_ROLES = Set.of("ROLE_STAFF", "ROLE_ADMIN");

    public boolean canAccess(Authentication authentication, Long customerId) {
        if (authentication == null) {
            return false;
        }
        boolean isStaff = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(STAFF_ROLES::contains);
        if (isStaff) {
            return true;
        }
        return authentication.getPrincipal() instanceof UserPrincipal principal
                && principal.getId().equals(customerId);
    }
}
