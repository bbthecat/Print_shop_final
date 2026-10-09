package com.printflow.service;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.response.OrderStatusHistoryResponse;

import java.util.List;
import java.util.Optional;

public interface OrderStatusService {

    OrderStatus changeStatus(Long orderId, OrderStatus target, Long actorUserId);

    List<OrderStatusHistoryResponse> getHistories(Long orderId);

    // UC-07: ลูกค้ายกเลิก order ของตัวเองได้เฉพาะตอนที่ยังเป็น PENDING
    OrderStatus cancelByCustomer(Long orderId, Long customerId);

    // สถานะถัดไปที่เปลี่ยนได้จากสถานะปัจจุบัน (ตามกฎของ State pattern)
    List<OrderStatus> getAllowedNextStatuses(OrderStatus current);

    // ขั้นถัดไปแบบเดินหน้า (ไม่นับการยกเลิก) ใช้กับปุ่มเลื่อนสถานะหลาย order พร้อมกัน
    Optional<OrderStatus> getNextStatus(OrderStatus current);
}