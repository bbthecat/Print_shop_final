package com.printflow.controller.api;

import com.printflow.config.SecurityConfig;
import com.printflow.domain.entity.User;
import com.printflow.domain.enums.Role;
import com.printflow.dto.response.CustomerResponse;
import com.printflow.exception.DuplicateResourceException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.security.CustomerAccessChecker;
import com.printflow.security.UserPrincipal;
import com.printflow.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@Import({SecurityConfig.class, CustomerAccessChecker.class})
class CustomerControllerTest {

    private static final String VALID_REGISTER_JSON = """
            {"username":"somchai","email":"somchai@example.com","password":"password123",
             "firstName":"Somchai","lastName":"Jaidee","phoneNumber":"0812345678","address":"Khon Kaen"}
            """;

    private static final String VALID_UPDATE_JSON = """
            {"email":"new@example.com","firstName":"Somsri","lastName":"Jaidee"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    private CustomerResponse sampleResponse(Long id) {
        return new CustomerResponse(id, "somchai", "somchai@example.com", "Somchai", "Jaidee",
                "0812345678", "Khon Kaen", true, LocalDateTime.now());
    }

    private UserPrincipal customerPrincipal(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("customer" + id);
        user.setPasswordHash("hashed");
        user.setRole(Role.CUSTOMER);
        return new UserPrincipal(user);
    }

    @Test
    void register_validBody_returns201WithLocation() throws Exception {
        when(customerService.register(any())).thenReturn(sampleResponse(1L));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTER_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/customers/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void register_invalidBody_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"ab","email":"not-an-email","password":"short",
                                 "firstName":"","lastName":"Jaidee"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.username").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists())
                .andExpect(jsonPath("$.validationErrors.firstName").exists());
        verify(customerService, never()).register(any());
    }

    @Test
    void register_duplicate_returns409() throws Exception {
        when(customerService.register(any()))
                .thenThrow(new DuplicateResourceException("Username already exists: somchai"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTER_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists: somchai"))
                .andExpect(jsonPath("$.path").value("/api/v1/customers"));
    }

    @Test
    void getById_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void getById_asStaff_returns200() throws Exception {
        when(customerService.getById(1L)).thenReturn(sampleResponse(1L));

        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("somchai"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void getById_notFound_returns404() throws Exception {
        when(customerService.getById(99L)).thenThrow(new ResourceNotFoundException("Customer not found: 99"));

        mockMvc.perform(get("/api/v1/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getById_ownAccount_returns200() throws Exception {
        when(customerService.getById(5L)).thenReturn(sampleResponse(5L));

        mockMvc.perform(get("/api/v1/customers/5").with(user(customerPrincipal(5L))))
                .andExpect(status().isOk());
    }

    @Test
    void getById_anotherCustomer_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/customers/6").with(user(customerPrincipal(5L))))
                .andExpect(status().isForbidden());
        verify(customerService, never()).getById(any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAll_asCustomer_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void getAll_asStaff_passesPagingAndSorting() throws Exception {
        when(customerService.getAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse(1L))));

        mockMvc.perform(get("/api/v1/customers")
                        .param("page", "0").param("size", "5").param("sort", "username,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("somchai"));
        verify(customerService).getAll(PageRequest.of(0, 5, Sort.by("username").descending()));
    }

    @Test
    void update_ownAccount_returns200() throws Exception {
        when(customerService.update(eq(5L), any())).thenReturn(sampleResponse(5L));

        mockMvc.perform(put("/api/v1/customers/5").with(user(customerPrincipal(5L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_UPDATE_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_asAdmin_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/1"))
                .andExpect(status().isNoContent());
        verify(customerService).deactivate(1L);
    }

    @Test
    void delete_asCustomer_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/5").with(user(customerPrincipal(5L))))
                .andExpect(status().isForbidden());
        verify(customerService, never()).deactivate(any());
    }
}
