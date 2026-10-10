package com.printflow.controller.api;

import com.printflow.dto.request.CustomerRegisterRequest;
import com.printflow.dto.request.CustomerUpdateRequest;
import com.printflow.dto.response.CustomerResponse;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@Tag(name = "Customers", description = "สมัครสมาชิกและจัดการข้อมูลลูกค้า")
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CurrentUserProvider currentUserProvider;

    public CustomerController(CustomerService customerService, CurrentUserProvider currentUserProvider) {
        this.customerService = customerService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @Operation(summary = "สมัครสมาชิก (ไม่ต้อง login)")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody CustomerRegisterRequest request) {
        CustomerResponse created = customerService.register(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "รายการลูกค้า แบ่งหน้า/เรียงลำดับ (STAFF, ADMIN)")
    public ResponseEntity<Page<CustomerResponse>> getAll(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(customerService.getAll(pageable));
    }

    @GetMapping("/me")
    @Operation(summary = "ดูข้อมูลของตัวเอง (ลูกค้าที่ login อยู่)")
    public ResponseEntity<CustomerResponse> getMe() {
        return ResponseEntity.ok(customerService.getById(currentUserProvider.getCurrentUserId()));
    }

    @PutMapping("/me")
    @Operation(summary = "แก้ไขข้อมูลของตัวเอง (ลูกค้าที่ login อยู่)")
    public ResponseEntity<CustomerResponse> updateMe(@Valid @RequestBody CustomerUpdateRequest request) {
        return ResponseEntity.ok(customerService.update(currentUserProvider.getCurrentUserId(), request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "ดูข้อมูลลูกค้า (STAFF, ADMIN)")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "แก้ไขข้อมูลลูกค้า (STAFF, ADMIN)")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CustomerUpdateRequest request) {
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "ปิดบัญชีลูกค้าแบบ soft delete (ADMIN)")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        customerService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
