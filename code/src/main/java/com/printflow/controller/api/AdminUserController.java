package com.printflow.controller.api;

import com.printflow.domain.enums.Role;
import com.printflow.dto.request.AdminUserCreateRequest;
import com.printflow.dto.request.UserRoleUpdateRequest;
import com.printflow.dto.request.UserStatusUpdateRequest;
import com.printflow.dto.response.UserResponse;
import com.printflow.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@Tag(name = "Admin - Users", description = "จัดการบัญชีผู้ใช้ทุก role (ADMIN)")
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @Operation(summary = "รายการผู้ใช้ กรองตาม role ได้ แบ่งหน้า/เรียงลำดับ")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) Role role,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getUsers(role, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "ดูข้อมูลผู้ใช้")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getById(id));
    }

    @PostMapping
    @Operation(summary = "สร้างบัญชีผู้ใช้ (เช่น STAFF)")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody AdminUserCreateRequest request) {
        UserResponse created = adminUserService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "เปลี่ยน role ของผู้ใช้")
    public ResponseEntity<UserResponse> changeRole(@PathVariable Long id,
                                                   @Valid @RequestBody UserRoleUpdateRequest request) {
        return ResponseEntity.ok(adminUserService.changeRole(id, request.role()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "เปิด/ปิดบัญชีผู้ใช้")
    public ResponseEntity<UserResponse> changeStatus(@PathVariable Long id,
                                                     @Valid @RequestBody UserStatusUpdateRequest request) {
        return ResponseEntity.ok(adminUserService.changeActive(id, request.active()));
    }
}
