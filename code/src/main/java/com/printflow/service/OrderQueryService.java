package com.printflow.service;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderQueryService {

    OrderResponse getById(Long id);

    Page<OrderResponse> getAll(Pageable pageable);

    Page<OrderResponse> getAllByStatus(
            OrderStatus status,
            Pageable pageable
    );
}

