package com.printflow.controller.api;

import com.printflow.domain.entity.Promotion;
import com.printflow.dto.request.PromotionRequest;
import com.printflow.dto.response.PromotionResponse;
import com.printflow.mapper.PromotionMapper;
import com.printflow.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Tag(name = "Promotions", description = "จัดการรายการโปรโมชันและคูปองส่วนลด")
@RequestMapping("/api/v1/promotions")
public class PromotionController {

    private final PromotionService promotionService;
    private final PromotionMapper mapper;

    // Constructor Injection ตามเกณฑ์ห้าม @Autowired บน field
    public PromotionController(PromotionService promotionService, PromotionMapper mapper) {
        this.promotionService = promotionService;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "ดึงรายการโปรโมชันทั้งหมดที่เปิดใช้งาน")
    public ResponseEntity<List<PromotionResponse>> getAll() {
        List<PromotionResponse> list = promotionService.findAllActive().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "ดึงข้อมูลโปรโมชันตาม ID")
    public ResponseEntity<PromotionResponse> getById(@PathVariable Long id) {
        Promotion promotion = promotionService.findById(id);
        return ResponseEntity.ok(mapper.toResponse(promotion));
    }

    @GetMapping("/validate/{code}")
    @Operation(summary = "ตรวจสอบความถูกต้องของรหัสคูปองโปรโมชัน")
    public ResponseEntity<PromotionResponse> validateCode(@PathVariable String code) {
        Promotion promotion = promotionService.findValidByCode(code);
        return ResponseEntity.ok(mapper.toResponse(promotion));
    }

    @PostMapping
    @Operation(summary = "สร้างโปรโมชันใหม่ (คืน 201 Created)")
    public ResponseEntity<PromotionResponse> create(@Valid @RequestBody PromotionRequest request) {
        Promotion created = promotionService.create(
                request.code(),
                request.description(),
                request.discountType(),
                request.discountValue(),
                request.minOrderAmount(),
                request.startDate(),
                request.endDate()
        );
        PromotionResponse response = mapper.toResponse(created);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "ปิดการใช้งานโปรโมชันแบบ Soft Delete (คืน 204 No Content)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promotionService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
