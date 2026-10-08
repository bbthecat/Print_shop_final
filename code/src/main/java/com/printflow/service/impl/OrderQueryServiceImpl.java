package com.printflow.service.impl;

import com.printflow.domain.entity.PrintItem;
import com.printflow.domain.entity.PrintItemAddon;
import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.response.OrderResponse;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.mapper.OrderMapper;
import com.printflow.repository.OrderRepository;
import com.printflow.repository.PrintItemAddonRepository;
import com.printflow.repository.PrintItemRepository;
import com.printflow.service.OrderQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final PrintItemRepository printItemRepository;
    private final PrintItemAddonRepository printItemAddonRepository;
    private final OrderMapper orderMapper;

    public OrderQueryServiceImpl(
            OrderRepository orderRepository,
            PrintItemRepository printItemRepository,
            PrintItemAddonRepository printItemAddonRepository,
            OrderMapper orderMapper
    ) {
        this.orderRepository = orderRepository;
        this.printItemRepository = printItemRepository;
        this.printItemAddonRepository = printItemAddonRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    public OrderResponse getById(Long id) {
        PrintOrder order = findOrder(id);

        List<PrintItem> items =
                printItemRepository.findByOrderId(id);

        Map<Long, List<Long>> addonIdsByItemId =
                getAddonIdsByItemId(items);

        return orderMapper.toResponse(
                order,
                items,
                addonIdsByItemId
        );
    }

    @Override
    public Page<OrderResponse> getAll(Pageable pageable) {
        return mapOrders(orderRepository.findAll(pageable));
    }

    @Override
    public Page<OrderResponse> getAllByStatus(
            OrderStatus status,
            Pageable pageable
    ) {
        return mapOrders(
                orderRepository.findByStatus(status, pageable)
        );
    }

    private Page<OrderResponse> mapOrders(Page<PrintOrder> orders) {
        return orders.map(order -> {
            List<PrintItem> items =
                    printItemRepository.findByOrderId(order.getId());

            Map<Long, List<Long>> addonIdsByItemId =
                    getAddonIdsByItemId(items);

            return orderMapper.toResponse(
                    order,
                    items,
                    addonIdsByItemId
            );
        });
    }

    private PrintOrder findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found: " + id
                        )
                );
    }

    private Map<Long, List<Long>> getAddonIdsByItemId(
            List<PrintItem> items
    ) {
        return items.stream()
                .flatMap(item ->
                        printItemAddonRepository
                                .findByItemId(item.getId())
                                .stream()
                                .map(addon -> Map.entry(
                                        item.getId(),
                                        addon.getAddonId()
                                ))
                )
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(
                                Map.Entry::getValue,
                                Collectors.toList()
                        )
                ));
    }
}

