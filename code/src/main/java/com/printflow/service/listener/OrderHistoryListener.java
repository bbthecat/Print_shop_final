package com.printflow.service.listener;

import com.printflow.domain.entity.OrderStatusHistory;
import com.printflow.repository.OrderStatusHistoryRepository;
import com.printflow.service.event.OrderStatusChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderHistoryListener {

    private final OrderStatusHistoryRepository historyRepository;

    public OrderHistoryListener(OrderStatusHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @EventListener
    public void onStatusChanged(OrderStatusChangedEvent event) {
        historyRepository.save(new OrderStatusHistory(
                event.orderId(),
                event.oldStatus(),
                event.newStatus(),
                event.changedBy()
        ));
    }
}
