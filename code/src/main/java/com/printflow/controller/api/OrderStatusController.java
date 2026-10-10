package com.printflow.controller.api;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.request.OrderStatusUpdateRequest;
import com.printflow.dto.response.OrderStatusHistoryResponse;
import com.printflow.dto.response.OrderStatusResponse;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.OrderQueryService;
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
    private final OrderQueryService orderQueryService;
    private final CurrentUserProvider currentUserProvider;

    public OrderStatusController(
            OrderStatusService orderStatusService,
            OrderQueryService orderQueryService,
            CurrentUserProvider currentUserProvider
    ) {
        this.orderStatusService = orderStatusService;
        this.orderQueryService = orderQueryService;
        this.currentUserProvider = currentUserProvider;
    }

    @PatchMapping("/status")
    @Operation(summary = "Change order status (STAFF, ADMIN)")
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

    @PostMapping("/cancel")
    @Operation(summary = "Customer cancels own PENDING order")
    public ResponseEntity<OrderStatusResponse> cancel(@PathVariable Long orderId) {
        OrderStatus status = orderStatusService.cancelByCustomer(
                orderId,
                currentUserProvider.getCurrentUserId()
        );
        return ResponseEntity.ok(new OrderStatusResponse(orderId, status));
    }

    @GetMapping("/status-histories")
    @Operation(summary = "Get order status history (own orders, or any for STAFF/ADMIN)")
    public ResponseEntity<List<OrderStatusHistoryResponse>> getHistories(
            @PathVariable Long orderId
    ) {
        // เช็กสิทธิ์ก่อน: ลูกค้าดูได้เฉพาะ order ของตัวเอง
        orderQueryService.getByIdForUser(
                orderId,
                currentUserProvider.getCurrentUserId(),
                currentUserProvider.isStaffOrAdmin()
        );
        return ResponseEntity.ok(orderStatusService.getHistories(orderId));
    }
}
