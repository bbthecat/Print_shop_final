package com.printflow.controller.api;

import com.printflow.dto.request.PaymentUpdateRequest;
import com.printflow.dto.response.PaymentResponse;
import com.printflow.mapper.PaymentMapper;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.OrderQueryService;
import com.printflow.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Payments", description = "บันทึกและตรวจสอบการชำระเงิน")
@RequestMapping("/api/v1/orders/{orderId}/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;
    private final OrderQueryService orderQueryService;
    private final CurrentUserProvider currentUserProvider;

    public PaymentController(
            PaymentService paymentService,
            PaymentMapper paymentMapper,
            OrderQueryService orderQueryService,
            CurrentUserProvider currentUserProvider
    ) {
        this.paymentService = paymentService;
        this.paymentMapper = paymentMapper;
        this.orderQueryService = orderQueryService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @Operation(summary = "Create an unpaid payment record using the order total (STAFF, ADMIN)")
    public ResponseEntity<PaymentResponse> create(@PathVariable Long orderId) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentMapper.toResponse(paymentService.createUnpaid(orderId)));
    }

    @GetMapping
    @Operation(summary = "Get payment of an order (own orders, or any for STAFF/ADMIN)")
    public ResponseEntity<PaymentResponse> get(@PathVariable Long orderId) {
        // เช็กสิทธิ์ก่อน: ลูกค้าดูได้เฉพาะ order ของตัวเอง
        orderQueryService.getByIdForUser(
                orderId,
                currentUserProvider.getCurrentUserId(),
                currentUserProvider.isStaffOrAdmin()
        );
        return ResponseEntity.ok(
                paymentMapper.toResponse(paymentService.findByOrderId(orderId)));
    }

    @PatchMapping
    @Operation(summary = "Mark payment as paid (STAFF, ADMIN)")
    public ResponseEntity<PaymentResponse> markAsPaid(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentUpdateRequest request
    ) {
        return ResponseEntity.ok(
                paymentMapper.toResponse(
                        paymentService.markAsPaid(orderId, request.paymentMethod())));
    }
}
