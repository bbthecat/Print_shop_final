package com.printflow.service;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.response.OrderFileResponse;
import com.printflow.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderQueryService {

    OrderResponse getById(Long id);

    /**
     * ดู order ได้เฉพาะของตัวเอง ยกเว้น STAFF/ADMIN (canViewAll = true) ที่ดูได้ทุก order
     */
    OrderResponse getByIdForUser(Long id, Long userId, boolean canViewAll);

    List<OrderFileResponse> getFiles(Long orderId);

    Page<OrderResponse> getAll(Pageable pageable);

    Page<OrderResponse> getAllByStatus(
            OrderStatus status,
            Pageable pageable
    );

    Page<OrderResponse> getAllByUserId(
            Long userId,
            Pageable pageable
    );
}
