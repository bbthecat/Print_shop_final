package com.printflow.service.impl;

import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.exception.InvalidStateTransitionException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.repository.OrderRepository;
import com.printflow.service.OrderStatusService;
import com.printflow.service.event.OrderStatusChangedEvent;
import com.printflow.service.state.OrderState;
import com.printflow.service.state.OrderStateResolver;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderStatusServiceImpl implements OrderStatusService {

    private final OrderRepository orderRepository;
    private final OrderStateResolver stateResolver;
    private final ApplicationEventPublisher eventPublisher;

    public OrderStatusServiceImpl(
            OrderRepository orderRepository,
            OrderStateResolver stateResolver,
            ApplicationEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.stateResolver = stateResolver;
        this.eventPublisher = eventPublisher;
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