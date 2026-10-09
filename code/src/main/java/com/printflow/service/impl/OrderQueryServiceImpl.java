package com.printflow.service.impl;

import com.printflow.domain.entity.AddonService;
import com.printflow.domain.entity.OrderPromotion;
import com.printflow.domain.entity.PrintItem;
import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.entity.PrintService;
import com.printflow.domain.entity.Promotion;
import com.printflow.domain.entity.User;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.response.OrderFileResponse;
import com.printflow.dto.response.OrderResponse;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.mapper.OrderMapper;
import com.printflow.repository.AddonServiceRepository;
import com.printflow.repository.OrderFileRepository;
import com.printflow.repository.OrderPromotionRepository;
import com.printflow.repository.OrderRepository;
import com.printflow.repository.PrintItemAddonRepository;
import com.printflow.repository.PrintItemRepository;
import com.printflow.repository.PrintServiceRepository;
import com.printflow.repository.PromotionRepository;
import com.printflow.repository.UserRepository;
import com.printflow.service.OrderQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final PrintItemRepository printItemRepository;
    private final PrintItemAddonRepository printItemAddonRepository;
    private final PrintServiceRepository printServiceRepository;
    private final AddonServiceRepository addonServiceRepository;
    private final UserRepository userRepository;
    private final OrderFileRepository orderFileRepository;
    private final OrderPromotionRepository orderPromotionRepository;
    private final PromotionRepository promotionRepository;
    private final OrderMapper orderMapper;

    public OrderQueryServiceImpl(
            OrderRepository orderRepository,
            PrintItemRepository printItemRepository,
            PrintItemAddonRepository printItemAddonRepository,
            PrintServiceRepository printServiceRepository,
            AddonServiceRepository addonServiceRepository,
            UserRepository userRepository,
            OrderFileRepository orderFileRepository,
            OrderPromotionRepository orderPromotionRepository,
            PromotionRepository promotionRepository,
            OrderMapper orderMapper
    ) {
        this.orderRepository = orderRepository;
        this.printItemRepository = printItemRepository;
        this.printItemAddonRepository = printItemAddonRepository;
        this.printServiceRepository = printServiceRepository;
        this.addonServiceRepository = addonServiceRepository;
        this.userRepository = userRepository;
        this.orderFileRepository = orderFileRepository;
        this.orderPromotionRepository = orderPromotionRepository;
        this.promotionRepository = promotionRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    public OrderResponse getById(Long id) {
        return toResponse(findOrder(id));
    }

    @Override
    public OrderResponse getByIdForUser(Long id, Long userId, boolean canViewAll) {
        PrintOrder order = findOrder(id);
        if (!canViewAll && !order.getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only view your own orders");
        }
        return toResponse(order);
    }

    @Override
    public Set<Long> getUsedPromotionIds(Long userId) {
        return new HashSet<>(orderPromotionRepository.findUsedPromotionIds(userId, OrderStatus.CANCELLED));
    }

    @Override
    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    public List<OrderFileResponse> getFiles(Long orderId) {
        return orderFileRepository.findByOrderId(orderId).stream()
                .map(file -> new OrderFileResponse(
                        file.getId(), file.getFileName(), file.getFilePath(), file.getFileType()))
                .toList();
    }

    @Override
    public Page<OrderResponse> getAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public Page<OrderResponse> getAllByStatus(
            OrderStatus status,
            Pageable pageable
    ) {
        return orderRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    @Override
    public Page<OrderResponse> getAllByUserId(
            Long userId,
            Pageable pageable
    ) {
        return orderRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    private OrderResponse toResponse(PrintOrder order) {
        List<PrintItem> items = printItemRepository.findByOrderId(order.getId());
        Map<Long, List<Long>> addonIdsByItemId = getAddonIdsByItemId(items);

        // ดึงชื่อบริการ/บริการเสริมมาแสดงแทน id (รวมตัวที่ปิดใช้งานแล้วด้วย)
        Map<Long, String> serviceNames = printServiceRepository
                .findAllById(items.stream().map(PrintItem::getServiceId).toList())
                .stream()
                .collect(Collectors.toMap(PrintService::getId, PrintService::getName));
        Map<Long, String> addonNames = addonServiceRepository
                .findAllById(addonIdsByItemId.values().stream().flatMap(List::stream).toList())
                .stream()
                .collect(Collectors.toMap(AddonService::getId, AddonService::getName));

        String customerName = userRepository.findById(order.getUserId())
                .map(User::getUsername)
                .orElse("#" + order.getUserId());

        // ส่วนลดที่ใช้กับ order นี้ (มีได้ไม่เกิน 1 โค้ด)
        List<OrderPromotion> promotions = orderPromotionRepository.findByOrderId(order.getId());
        BigDecimal discount = promotions.stream()
                .map(OrderPromotion::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String promotionCode = promotions.isEmpty()
                ? null
                : promotionRepository.findById(promotions.get(0).getPromotionId())
                        .map(Promotion::getCode)
                        .orElse(null);

        return orderMapper.toResponse(order, items, addonIdsByItemId, serviceNames, addonNames, customerName,
                discount, promotionCode);
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
