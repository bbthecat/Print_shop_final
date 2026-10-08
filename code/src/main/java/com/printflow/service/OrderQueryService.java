package com.printflow.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.printflow.dto.response.OrderResponse;

public interface OrderQueryService {

    OrderResponse getById(Long id);

    Page<OrderResponse> getAll(Pageable pageable);
}