package com.printflow.service;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.response.OrderStatusHistoryResponse;

import java.util.List;

public interface OrderStatusService {

    OrderStatus changeStatus(Long orderId, OrderStatus target, Long actorUserId);

    List<OrderStatusHistoryResponse> getHistories(Long orderId);
}