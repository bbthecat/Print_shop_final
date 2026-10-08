package com.printflow.controller.api;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.request.OrderStatusUpdateRequest;
import com.printflow.dto.response.OrderStatusHistoryResponse;
import com.printflow.dto.response.OrderStatusResponse;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.OrderStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Order Status", description = "เปลี่ยนสถานะและดูประวัติสถานะคำสั่งซื้อ")
@RequestMapping("/api/v1/orders/{orderId}")
public class OrderStatusController {

    private final OrderStatusService orderStatusService;
    private final CurrentUserProvider currentUserProvider;

    public OrderStatusController(
            OrderStatusService orderStatusService,
            CurrentUserProvider currentUserProvider
    ) {
        this.orderStatusService = orderStatusService;
        this.currentUserProvider = currentUserProvider;
    }

    @PatchMapping("/status")
    @Operation(summary = "Change order status")
    public ResponseEntity<OrderStatusResponse> changeStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderStatus status = orderStatusService.changeStatus(
                orderId,
                request.status(),
                currentUserProvider.getCurrentUserId()
        );

        return ResponseEntity.ok(new OrderStatusResponse(orderId, status));
    }

    @GetMapping("/status-histories")
    @Operation(summary = "Get order status history")
    public ResponseEntity<List<OrderStatusHistoryResponse>> getHistories(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(orderStatusService.getHistories(orderId));
    }
}