package com.printflow.controller.api;

import com.printflow.dto.request.PaymentCreateRequest;
import com.printflow.dto.request.PaymentUpdateRequest;
import com.printflow.dto.response.PaymentResponse;
import com.printflow.mapper.PaymentMapper;
import com.printflow.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Payments", description = "บันทึกและตรวจสอบการชำระเงิน")
@RequestMapping("/api/v1/orders/{orderId}/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    public PaymentController(PaymentService paymentService, PaymentMapper paymentMapper) {
        this.paymentService = paymentService;
        this.paymentMapper = paymentMapper;
    }

    @PostMapping
    @Operation(summary = "Create an unpaid payment record for an order")
    public ResponseEntity<PaymentResponse> create(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentMapper.toResponse(
                        paymentService.createUnpaid(orderId, request.amount())));
    }

    @GetMapping
    @Operation(summary = "Get payment of an order")
    public ResponseEntity<PaymentResponse> get(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                paymentMapper.toResponse(paymentService.findByOrderId(orderId)));
    }

    @PatchMapping
    @Operation(summary = "Mark payment as paid")
    public ResponseEntity<PaymentResponse> markAsPaid(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentUpdateRequest request
    ) {
        return ResponseEntity.ok(
                paymentMapper.toResponse(
                        paymentService.markAsPaid(orderId, request.paymentMethod())));
    }
}