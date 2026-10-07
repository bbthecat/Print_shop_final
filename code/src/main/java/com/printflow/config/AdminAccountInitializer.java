package com.printflow.config;

import com.printflow.domain.enums.Role;
import com.printflow.dto.request.AdminUserCreateRequest;
import com.printflow.exception.DuplicateResourceException;
import com.printflow.service.AdminUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the first ADMIN account from environment variables so the system can be managed
 * after a fresh deploy. Credentials never live in the repository.
 */
@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final AdminUserService adminUserService;
    private final String username;
    private final String email;
    private final String password;

    public AdminAccountInitializer(AdminUserService adminUserService,
                                   @Value("${app.admin.username:}") String username,
                                   @Value("${app.admin.email:}") String email,
                                   @Value("${app.admin.password:}") String password) {
        this.adminUserService = adminUserService;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            log.info("ADMIN_USERNAME/ADMIN_EMAIL/ADMIN_PASSWORD not set, skipping admin bootstrap");
            return;
        }
        try {
            adminUserService.create(new AdminUserCreateRequest(
                    username, email, password, "System", "Admin", null, Role.ADMIN));
            log.info("Created bootstrap admin account '{}'", username);
        } catch (DuplicateResourceException ex) {
            log.info("Bootstrap admin account already exists, skipping");
        }
    }
}
