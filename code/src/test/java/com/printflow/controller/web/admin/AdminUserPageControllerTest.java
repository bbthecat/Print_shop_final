package com.printflow.controller.web.admin;

import com.printflow.config.SecurityConfig;
import com.printflow.domain.enums.Role;
import com.printflow.dto.response.UserResponse;
import com.printflow.exception.ValidationException;
import com.printflow.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminUserPageController.class)
@Import(SecurityConfig.class)
class AdminUserPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

    private UserResponse staff() {
        return new UserResponse(2L, "staff01", "staff01@example.com", "Somsak", "Staff",
                Role.STAFF, true, LocalDateTime.now());
    }

    @Test
    void usersPage_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/users").accept("text/html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void usersPage_asCustomer_returns403() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void usersPage_asAdmin_rendersUserTable() throws Exception {
        when(adminUserService.getUsers(isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(staff())));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(content().string(containsString("staff01@example.com")))
                .andExpect(content().string(containsString("จัดการผู้ใช้")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void usersPage_filterByRole_passesRoleToService() throws Exception {
        when(adminUserService.getUsers(eq(Role.STAFF), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(staff())));

        mockMvc.perform(get("/admin/users").param("role", "STAFF"))
                .andExpect(status().isOk());
        verify(adminUserService).getUsers(eq(Role.STAFF), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_valid_redirectsWithSuccess() throws Exception {
        mockMvc.perform(post("/admin/users").with(csrf())
                        .param("username", "staff01")
                        .param("email", "staff01@example.com")
                        .param("password", "password123")
                        .param("firstName", "Somsak")
                        .param("lastName", "Staff")
                        .param("role", "STAFF"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("success"));
        verify(adminUserService).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_invalid_redisplaysPageWithErrors() throws Exception {
        when(adminUserService.getUsers(isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(post("/admin/users").with(csrf())
                        .param("username", "")
                        .param("role", "STAFF"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeHasFieldErrors("form", "username", "email", "password"))
                .andExpect(model().attribute("openCreateForm", true));
        verify(adminUserService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeRole_ofSelf_redirectsWithError() throws Exception {
        when(adminUserService.changeRole(1L, Role.CUSTOMER))
                .thenThrow(new ValidationException("You cannot change your own role"));

        mockMvc.perform(post("/admin/users/1/role").with(csrf()).param("role", "CUSTOMER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "You cannot change your own role"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeStatus_deactivatesUser() throws Exception {
        mockMvc.perform(post("/admin/users/2/status").with(csrf()).param("active", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("success"));
        verify(adminUserService).changeActive(2L, false);
    }
}
