package com.printflow.service;

import com.printflow.dto.request.OrderCreateRequest;
import com.printflow.dto.response.OrderResponse;

public interface OrderCommandService {

    // userId = คนที่ login อยู่ (ไม่รับจาก request เพื่อไม่ให้สั่งในชื่อคนอื่นได้)
    OrderResponse createOrder(Long userId, OrderCreateRequest request);

    void deleteOrder(Long id);
}