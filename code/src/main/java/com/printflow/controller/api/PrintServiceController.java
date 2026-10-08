package com.printflow.controller.api;

import com.printflow.domain.entity.PrintService;
import com.printflow.dto.request.PrintServiceRequest;
import com.printflow.dto.response.PrintServiceResponse;
import com.printflow.mapper.ServiceCatalogMapper;
import com.printflow.service.ServiceCatalogCommandService;
import com.printflow.service.ServiceCatalogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@Tag(name = "Services", description = "จัดการรายการบริการงานพิมพ์")
@RequestMapping("/api/v1/services")
public class PrintServiceController {

    private final ServiceCatalogQueryService queryService;
    private final ServiceCatalogCommandService commandService;
    private final ServiceCatalogMapper mapper;

    // Constructor Injection ตามเกณฑ์ห้าม @Autowired บน field
    public PrintServiceController(ServiceCatalogQueryService queryService,
                                  ServiceCatalogCommandService commandService,
                                  ServiceCatalogMapper mapper) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "ดึงรายการบริการงานพิมพ์ แบ่งหน้าและเรียงลำดับ (Pagination & Sorting)")
    public ResponseEntity<Page<PrintServiceResponse>> getAll(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<PrintServiceResponse> responsePage = queryService.findAllActivePrintServices(pageable)
                .map(mapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "ดึงข้อมูลบริการงานพิมพ์ตาม ID")
    public ResponseEntity<PrintServiceResponse> getById(@PathVariable Long id) {
        PrintService service = queryService.findActivePrintServiceById(id);
        return ResponseEntity.ok(mapper.toResponse(service));
    }

    @PostMapping
    @Operation(summary = "สร้างบริการงานพิมพ์ใหม่ (คืน 201 Created)")
    public ResponseEntity<PrintServiceResponse> create(@Valid @RequestBody PrintServiceRequest request) {
        PrintService created = commandService.createPrintService(
                request.name(),
                request.description(),
                request.basePrice(),
                request.pricingType()
        );
        PrintServiceResponse response = mapper.toResponse(created);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "แก้ไขข้อมูลบริการงานพิมพ์ (คืน 200 OK)")
    public ResponseEntity<PrintServiceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PrintServiceRequest request) {
        PrintService updated = commandService.updatePrintService(
                id,
                request.name(),
                request.description(),
                request.basePrice(),
                request.pricingType(),
                null
        );
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "ปิดการใช้งานบริการงานพิมพ์แบบ Soft Delete (คืน 204 No Content)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.deactivatePrintService(id);
        return ResponseEntity.noContent().build();
    }
}
