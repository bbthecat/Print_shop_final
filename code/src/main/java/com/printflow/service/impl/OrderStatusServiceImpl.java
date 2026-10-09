package com.printflow.service.impl;

import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.entity.User;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.response.OrderStatusHistoryResponse;
import com.printflow.exception.InvalidStateTransitionException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.repository.OrderRepository;
import com.printflow.repository.OrderStatusHistoryRepository;
import com.printflow.repository.UserRepository;
import com.printflow.service.OrderStatusService;
import com.printflow.service.event.OrderStatusChangedEvent;
import com.printflow.service.state.OrderState;
import com.printflow.service.state.OrderStateResolver;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderStatusServiceImpl implements OrderStatusService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final OrderStateResolver stateResolver;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    public OrderStatusServiceImpl(
            OrderRepository orderRepository,
            OrderStatusHistoryRepository historyRepository,
            OrderStateResolver stateResolver,
            ApplicationEventPublisher eventPublisher,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.stateResolver = stateResolver;
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
    }

    @Override
    public OrderStatus changeStatus(Long orderId, OrderStatus target, Long actorUserId) {
        PrintOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        OrderStatus current = order.getStatus();
        OrderState state = stateResolver.resolve(current);
        OrderStatus next = applyAction(state, target);

        order.setStatus(next);
        orderRepository.save(order);

        eventPublisher.publishEvent(
                new OrderStatusChangedEvent(orderId, current, next, actorUserId));

        return next;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getHistories(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }

        return historyRepository.findByOrderIdOrderByChangedAtAsc(orderId)
                .stream()
                .map(history -> new OrderStatusHistoryResponse(
                        history.getId(),
                        history.getOldStatus(),
                        history.getNewStatus(),
                        history.getChangedBy(),
                        userRepository.findById(history.getChangedBy())
                                .map(User::getUsername)
                                .orElse("#" + history.getChangedBy()),
                        history.getChangedAt()
                ))
                .toList();
    }

    @Override
    public OrderStatus cancelByCustomer(Long orderId, Long customerId) {
        PrintOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!order.getUserId().equals(customerId)) {
            throw new AccessDeniedException("You can only cancel your own orders");
        }
        // หลังร้านรับงานแล้ว ลูกค้ายกเลิกเองไม่ได้ ต้องติดต่อร้าน
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidStateTransitionException(
                    "ยกเลิกได้เฉพาะคำสั่งที่ยังรอร้านยืนยัน (PENDING) เท่านั้น");
        }
        return changeStatus(orderId, OrderStatus.CANCELLED, customerId);
    }

    @Override
    public List<OrderStatus> getAllowedNextStatuses(OrderStatus current) {
        // ถาม State ของสถานะปัจจุบันว่าไปสถานะไหนได้บ้าง (ตัวไหนโยน exception = ไปไม่ได้)
        OrderState state = stateResolver.resolve(current);
        List<OrderStatus> allowed = new ArrayList<>();
        for (OrderStatus target : OrderStatus.values()) {
            try {
                applyAction(state, target);
                allowed.add(target);
            } catch (InvalidStateTransitionException ignored) {
                // สถานะนี้ไปไม่ได้
            }
        }
        return allowed;
    }

    private OrderStatus applyAction(OrderState state, OrderStatus target) {
        return switch (target) {
            case CONFIRMED -> state.confirm();
            case PROCESSING -> state.process();
            case READY -> state.ready();
            case COMPLETED -> state.complete();
            case CANCELLED -> state.cancel();
            case PENDING -> throw new InvalidStateTransitionException(
                    "Cannot change status from " + state.getStatus() + " to PENDING");
        };
    }
}