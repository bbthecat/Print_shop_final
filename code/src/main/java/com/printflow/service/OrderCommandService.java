package com.printflow.service;

import com.printflow.dto.request.OrderCreateRequest;
import com.printflow.dto.response.OrderResponse;

public interface OrderCommandService {

    OrderResponse createOrder(OrderCreateRequest request);

    void deleteOrder(Long id);
}