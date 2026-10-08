package com.printflow.controller.api;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.request.OrderCreateRequest;
import com.printflow.dto.response.OrderResponse;
import com.printflow.service.OrderCommandService;
import com.printflow.service.OrderQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Orders", description = "จัดการคำสั่งซื้อ")
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderCommandService commandService;
    private final OrderQueryService queryService;

    public OrderController(
            OrderCommandService commandService,
            OrderQueryService queryService
    ) {
        this.commandService = commandService;
        this.queryService = queryService;
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
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(queryService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create an order")
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody OrderCreateRequest request
    ) {
        return ResponseEntity.ok(
                commandService.createOrder(request)
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
}

