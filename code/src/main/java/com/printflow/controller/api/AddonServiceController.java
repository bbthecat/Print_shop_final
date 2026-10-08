package com.printflow.controller.api;

import com.printflow.domain.entity.AddonService;
import com.printflow.dto.request.AddonServiceRequest;
import com.printflow.dto.response.AddonServiceResponse;
import com.printflow.mapper.ServiceCatalogMapper;
import com.printflow.service.ServiceCatalogCommandService;
import com.printflow.service.ServiceCatalogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Tag(name = "Addon Services", description = "จัดการรายการบริการเสริม (เข้าเล่ม เคลือบ เย็บเล่ม)")
@RequestMapping("/api/v1/addon-services")
public class AddonServiceController {

    private final ServiceCatalogQueryService queryService;
    private final ServiceCatalogCommandService commandService;
    private final ServiceCatalogMapper mapper;

    // Constructor Injection ตามเกณฑ์ห้าม @Autowired บน field
    public AddonServiceController(ServiceCatalogQueryService queryService,
                                  ServiceCatalogCommandService commandService,
                                  ServiceCatalogMapper mapper) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "ดึงรายการบริการเสริมทั้งหมดที่เปิดใช้งาน")
    public ResponseEntity<List<AddonServiceResponse>> getAll() {
        List<AddonServiceResponse> list = queryService.findAllActiveAddonServices().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "ดึงข้อมูลบริการเสริมตาม ID")
    public ResponseEntity<AddonServiceResponse> getById(@PathVariable Long id) {
        AddonService addon = queryService.findActiveAddonServiceById(id);
        return ResponseEntity.ok(mapper.toResponse(addon));
    }

    @PostMapping
    @Operation(summary = "สร้างบริการเสริมใหม่ (คืน 201 Created)")
    public ResponseEntity<AddonServiceResponse> create(@Valid @RequestBody AddonServiceRequest request) {
        AddonService created = commandService.createAddonService(
                request.name(),
                request.description(),
                request.price()
        );
        AddonServiceResponse response = mapper.toResponse(created);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "แก้ไขข้อมูลบริการเสริม (คืน 200 OK)")
    public ResponseEntity<AddonServiceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AddonServiceRequest request) {
        AddonService updated = commandService.updateAddonService(
                id,
                request.name(),
                request.description(),
                request.price(),
                null
        );
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "ปิดการใช้งานบริการเสริมแบบ Soft Delete (คืน 204 No Content)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.deactivateAddonService(id);
        return ResponseEntity.noContent().build();
    }
}
