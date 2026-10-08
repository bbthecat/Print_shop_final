package com.printflow.mapper;

import com.printflow.domain.entity.PrintItem;
import com.printflow.domain.entity.PrintOrder;
import com.printflow.dto.response.OrderItemResponse;
import com.printflow.dto.response.OrderResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class OrderMapper {

    public OrderResponse toResponse(
            PrintOrder order,
            List<PrintItem> items,
            Map<Long, List<Long>> addonIdsByItemId
    ) {
        List<OrderItemResponse> itemResponses =
                items == null
                        ? Collections.emptyList()
                        : items.stream()
                                .map(item -> toItemResponse(
                                        item,
                                        addonIdsByItemId.getOrDefault(
                                                item.getId(),
                                                Collections.emptyList()
                                        )
                                ))
                                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                itemResponses
        );
    }

    public OrderItemResponse toItemResponse(
            PrintItem item,
            List<Long> addonIds
    ) {
        return new OrderItemResponse(
                item.getId(),
                item.getServiceId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal(),
                addonIds,
                item.getPageCount() != null ? item.getPageCount() : 1
        );
    }
}