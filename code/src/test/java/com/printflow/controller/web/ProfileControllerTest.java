package com.printflow.controller.web;

import com.printflow.config.SecurityConfig;
import com.printflow.dto.request.CustomerUpdateRequest;
import com.printflow.dto.response.CustomerResponse;
import com.printflow.exception.DuplicateResourceException;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    private CustomerResponse customer() {
        return new CustomerResponse(5L, "somchai", "somchai@example.com", "Somchai", "Jaidee",
                "0812345678", "Khon Kaen", true, LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void profile_asStaff_returns403() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void profile_asCustomer_showsCurrentData() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(5L);
        when(customerService.getById(5L)).thenReturn(customer());

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/index"))
                .andExpect(content().string(containsString("somchai@example.com")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void update_validForm_savesAndRedirects() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(5L);

        mockMvc.perform(post("/profile").with(csrf())
                        .param("email", "new@example.com")
                        .param("firstName", "Somchai")
                        .param("lastName", "Jaidee"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("success"));

        verify(customerService).update(eq(5L), any(CustomerUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void update_invalidEmail_showsFieldError() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(5L);
        when(customerService.getById(5L)).thenReturn(customer());

        mockMvc.perform(post("/profile").with(csrf())
                        .param("email", "not-an-email")
                        .param("firstName", "Somchai")
                        .param("lastName", "Jaidee"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "email"));

        verify(customerService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void update_duplicateEmail_showsError() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(5L);
        when(customerService.getById(5L)).thenReturn(customer());
        when(customerService.update(eq(5L), any(CustomerUpdateRequest.class)))
                .thenThrow(new DuplicateResourceException("Email already exists"));

        mockMvc.perform(post("/profile").with(csrf())
                        .param("email", "taken@example.com")
                        .param("firstName", "Somchai")
                        .param("lastName", "Jaidee"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Email already exists"));
    }
}
