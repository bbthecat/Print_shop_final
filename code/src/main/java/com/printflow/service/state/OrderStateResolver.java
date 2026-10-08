package com.printflow.service.state;

import com.printflow.domain.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * แปลงค่า OrderStatus ที่เก็บในฐานข้อมูล ให้กลายเป็น OrderState object ที่มีกฎอยู่ในตัว
 *
 * ใช้ Map แทน switch-case เพื่อให้การเพิ่มสถานะใหม่
 * ทำได้ด้วยการเพิ่มคลาสแล้วมาต่อท้าย List ข้างล่าง
 * ไม่ต้องไล่แก้ switch ที่กระจายอยู่ตามที่ต่าง ๆ
 *
 * state object ทุกตัวไม่มี field ที่เปลี่ยนค่าได้ (stateless)
 * จึงสร้างครั้งเดียวแล้วใช้ร่วมกันทุก request ได้อย่างปลอดภัย
 */
@Component
public class OrderStateResolver {

    private final Map<OrderStatus, OrderState> states;

    public OrderStateResolver() {
        Map<OrderStatus, OrderState> registry = new EnumMap<>(OrderStatus.class);
        List<OrderState> allStates = List.of(
                new PendingState(),
                new ConfirmedState(),
                new ProcessingState(),
                new ReadyState(),
                new CompletedState(),
                new CancelledState()
        );
        for (OrderState state : allStates) {
            registry.put(state.getStatus(), state);
        }
        this.states = Collections.unmodifiableMap(registry);
    }

    public OrderState resolve(OrderStatus status) {
        OrderState state = states.get(status);
        if (state == null) {
            // ไม่ใช่ความผิดของผู้ใช้ แต่แปลว่าเราเพิ่มค่าใน enum แล้วลืมสร้างคลาสของสถานะนั้น
            throw new IllegalArgumentException("No state registered for status: " + status);
        }
        return state;
    }
}
