package com.printflow.controller.web;

import com.printflow.config.SecurityConfig;
import com.printflow.dto.request.CustomerRegisterRequest;
import com.printflow.exception.DuplicateResourceException;
import com.printflow.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest({AuthController.class, HomeController.class})
@Import(SecurityConfig.class)
class AuthPageControllerTest {

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.printflow.service.ServiceCatalogQueryService catalogQueryService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    private MockHttpServletRequestBuilder validRegistration() {
        return post("/register").with(csrf())
                .param("username", "somchai")
                .param("email", "somchai@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("firstName", "Somchai")
                .param("lastName", "Jaidee");
    }

    @Test
    void homePage_isPublic() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("สมัครสมาชิก")));
    }

    @Test
    @WithMockUser(username = "somchai")
    void homePage_loggedIn_showsUsername() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ออกจากระบบ")))
                .andExpect(content().string(containsString("somchai")));
    }

    @Test
    void loginPage_isPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void loginPage_afterRegister_showsSuccessMessage() throws Exception {
        mockMvc.perform(get("/login").param("registered", ""))
                .andExpect(content().string(containsString("สมัครสมาชิกสำเร็จ")));
    }

    @Test
    void registerPage_isPublic() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    void register_valid_redirectsToLogin() throws Exception {
        mockMvc.perform(validRegistration())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
        verify(customerService).register(any(CustomerRegisterRequest.class));
    }

    @Test
    void register_invalid_redisplaysFormWithErrors() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "ab")
                        .param("email", "bad")
                        .param("password", "short")
                        .param("confirmPassword", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("form",
                        "username", "email", "password", "firstName", "lastName", "passwordConfirmed"));
        verify(customerService, never()).register(any());
    }

    @Test
    void register_duplicate_showsErrorMessage() throws Exception {
        when(customerService.register(any()))
                .thenThrow(new DuplicateResourceException("Username already exists: somchai"));

        mockMvc.perform(validRegistration())
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(content().string(containsString("Username already exists: somchai")));
    }

    @Test
    void register_withoutCsrfToken_isRejected() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "somchai"))
                .andExpect(status().isForbidden());
        verify(customerService, never()).register(any());
    }
}
