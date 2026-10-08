package com.printflow.service;

import com.printflow.domain.enums.OrderStatus;

public interface OrderStatusService {

    OrderStatus changeStatus(Long orderId, OrderStatus target, Long actorUserId);
}