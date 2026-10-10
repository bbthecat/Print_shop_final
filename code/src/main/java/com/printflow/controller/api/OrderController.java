package com.printflow.controller.api;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.request.OrderCreateRequest;
import com.printflow.dto.response.OrderFileResponse;
import com.printflow.dto.response.OrderItemResponse;
import com.printflow.dto.response.OrderResponse;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.OrderCommandService;
import com.printflow.service.OrderQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Orders", description = "จัดการคำสั่งซื้อ")
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderCommandService commandService;
    private final OrderQueryService queryService;
    private final CurrentUserProvider currentUserProvider;

    public OrderController(
            OrderCommandService commandService,
            OrderQueryService queryService,
            CurrentUserProvider currentUserProvider
    ) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @Operation(summary = "Get orders with pagination, sorting, and optional status filter")
    public ResponseEntity<Page<OrderResponse>> getAll(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        if (status != null) {
            return ResponseEntity.ok(
                    queryService.getAllByStatus(status, pageable)
            );
        }

        return ResponseEntity.ok(queryService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID (customers can only see their own orders)")
    public ResponseEntity<OrderResponse> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(findVisibleOrder(id));
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "Get print items of an order")
    public ResponseEntity<List<OrderItemResponse>> getItems(@PathVariable Long id) {
        return ResponseEntity.ok(findVisibleOrder(id).items());
    }

    @GetMapping("/{id}/files")
    @Operation(summary = "Get files attached to an order")
    public ResponseEntity<List<OrderFileResponse>> getFiles(@PathVariable Long id) {
        findVisibleOrder(id);
        return ResponseEntity.ok(queryService.getFiles(id));
    }

    @PostMapping
    @Operation(summary = "Create an order for the logged-in user")
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody OrderCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                commandService.createOrder(currentUserProvider.getCurrentUserId(), request)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an order")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        commandService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    // ลูกค้าดูได้เฉพาะ order ของตัวเอง STAFF/ADMIN ดูได้ทุก order
    private OrderResponse findVisibleOrder(Long id) {
        return queryService.getByIdForUser(
                id,
                currentUserProvider.getCurrentUserId(),
                currentUserProvider.isStaffOrAdmin()
        );
    }
}
