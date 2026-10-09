package com.printflow.mapper;

import com.printflow.domain.entity.PrintItem;
import com.printflow.domain.entity.PrintOrder;
import com.printflow.dto.response.OrderItemResponse;
import com.printflow.dto.response.OrderResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class OrderMapper {

    /**
     * @param addonIdsByItemId id ของบริการเสริมในแต่ละ item
     * @param serviceNames     ชื่อบริการพิมพ์ (key = serviceId)
     * @param addonNames       ชื่อบริการเสริม (key = addonId)
     * @param customerName     ชื่อผู้ใช้ของลูกค้าเจ้าของ order
     * @param discountAmount   ส่วนลดจากโปรโมชัน
     * @param promotionCode    โค้ดโปรโมชันที่ใช้ (null ถ้าไม่ได้ใช้)
     */
    public OrderResponse toResponse(
            PrintOrder order,
            List<PrintItem> items,
            Map<Long, List<Long>> addonIdsByItemId,
            Map<Long, String> serviceNames,
            Map<Long, String> addonNames,
            String customerName,
            BigDecimal discountAmount,
            String promotionCode
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
                                        ),
                                        serviceNames,
                                        addonNames
                                ))
                                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                customerName,
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                itemResponses,
                discountAmount,
                promotionCode
        );
    }

    public OrderItemResponse toItemResponse(
            PrintItem item,
            List<Long> addonIds,
            Map<Long, String> serviceNames,
            Map<Long, String> addonNames
    ) {
        List<String> names = addonIds.stream()
                .map(id -> addonNames.getOrDefault(id, "#" + id))
                .toList();

        return new OrderItemResponse(
                item.getId(),
                item.getServiceId(),
                serviceNames.getOrDefault(item.getServiceId(), "#" + item.getServiceId()),
                item.getPageCount(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal(),
                addonIds,
                names
        );
    }
}
