package com.printflow.controller.api;

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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@Import(SecurityConfig.class)
class AdminUserControllerTest {

    private static final String CREATE_STAFF_JSON = """
            {"username":"staff01","email":"staff01@example.com","password":"password123",
             "firstName":"Somsak","lastName":"Staff","role":"STAFF"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

    private UserResponse staffResponse() {
        return new UserResponse(2L, "staff01", "staff01@example.com", "Somsak", "Staff",
                Role.STAFF, true, LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_returns201() throws Exception {
        when(adminUserService.create(any())).thenReturn(staffResponse());

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_STAFF_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("STAFF"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void create_asStaff_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_STAFF_JSON))
                .andExpect(status().isForbidden());
        verify(adminUserService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_missingRole_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"staff01","email":"staff01@example.com","password":"password123",
                                 "firstName":"Somsak","lastName":"Staff"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.role").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_filtersByRoleParam() throws Exception {
        when(adminUserService.getUsers(eq(Role.STAFF), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(staffResponse())));

        mockMvc.perform(get("/api/v1/admin/users").param("role", "STAFF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("staff01"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_invalidRoleParam_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users").param("role", "BOSS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeRole_self_returns400() throws Exception {
        when(adminUserService.changeRole(1L, Role.CUSTOMER))
                .thenThrow(new ValidationException("You cannot change your own role"));

        mockMvc.perform(patch("/api/v1/admin/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"CUSTOMER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You cannot change your own role"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeStatus_asAdmin_returns200() throws Exception {
        when(adminUserService.changeActive(2L, false)).thenReturn(staffResponse());

        mockMvc.perform(patch("/api/v1/admin/users/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk());
        verify(adminUserService).changeActive(2L, false);
    }
}
