package com.printflow.service.impl;

import com.printflow.domain.entity.AddonService;
import com.printflow.domain.entity.OrderPromotion;
import com.printflow.domain.entity.PrintItem;
import com.printflow.domain.entity.PrintItemAddon;
import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.entity.PrintService;
import com.printflow.domain.entity.Promotion;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.request.OrderCreateRequest;
import com.printflow.dto.request.OrderItemRequest;
import com.printflow.dto.response.OrderResponse;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.mapper.OrderMapper;
import com.printflow.repository.OrderPromotionRepository;
import com.printflow.repository.OrderRepository;
import com.printflow.repository.PrintItemAddonRepository;
import com.printflow.repository.PrintItemRepository;
import com.printflow.service.OrderCommandService;
import com.printflow.service.PromotionService;
import com.printflow.service.ServiceCatalogQueryService;
import com.printflow.service.strategy.pricing.PricingCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OrderCommandServiceImpl implements OrderCommandService {

    private final OrderRepository orderRepository;
    private final PrintItemRepository printItemRepository;
    private final PrintItemAddonRepository printItemAddonRepository;
    private final OrderPromotionRepository orderPromotionRepository;
    private final ServiceCatalogQueryService serviceCatalogQueryService;
    private final PromotionService promotionService;
    private final PricingCalculator pricingCalculator;
    private final OrderMapper orderMapper;

    public OrderCommandServiceImpl(
            OrderRepository orderRepository,
            PrintItemRepository printItemRepository,
            PrintItemAddonRepository printItemAddonRepository,
            OrderPromotionRepository orderPromotionRepository,
            ServiceCatalogQueryService serviceCatalogQueryService,
            PromotionService promotionService,
            PricingCalculator pricingCalculator,
            OrderMapper orderMapper
    ) {
        this.orderRepository = orderRepository;
        this.printItemRepository = printItemRepository;
        this.printItemAddonRepository = printItemAddonRepository;
        this.orderPromotionRepository = orderPromotionRepository;
        this.serviceCatalogQueryService = serviceCatalogQueryService;
        this.promotionService = promotionService;
        this.pricingCalculator = pricingCalculator;
        this.orderMapper = orderMapper;
    }

    @Override
    public OrderResponse createOrder(OrderCreateRequest request) {

        PrintOrder order = new PrintOrder(
                generateOrderNumber(),
                request.userId(),
                OrderStatus.PENDING,
                BigDecimal.ZERO
        );

        order = orderRepository.save(order);

        BigDecimal orderTotal = BigDecimal.ZERO;
        List<PrintItem> items = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.items()) {

            PrintService printService =
                    serviceCatalogQueryService.findActivePrintServiceById(
                            itemRequest.serviceId()
                    );

            List<BigDecimal> addonPrices = new ArrayList<>();
            List<AddonService> addons = new ArrayList<>();

            if (itemRequest.addonIds() != null) {
                for (Long addonId : itemRequest.addonIds()) {
                    AddonService addon =
                            serviceCatalogQueryService.findActiveAddonServiceById(
                                    addonId
                            );

                    addons.add(addon);
                    addonPrices.add(addon.getPrice());
                }
            }

            int pageCount = 1;
            int copyCount = Math.max(1, itemRequest.quantity());

            BigDecimal itemTotal =
                    pricingCalculator.calculateItemTotal(
                            printService.getPricingType(),
                            printService.getBasePrice(),
                            pageCount,
                            copyCount,
                            addonPrices
                    );

            BigDecimal unitPrice = itemTotal.divide(
                    BigDecimal.valueOf(copyCount),
                    2,
                    RoundingMode.HALF_UP
            );

            PrintItem item = new PrintItem(
                    order,
                    printService.getId(),
                    itemRequest.quantity(),
                    unitPrice,
                    itemTotal
            );

            item = printItemRepository.save(item);
            items.add(item);

            for (AddonService addon : addons) {
                PrintItemAddon itemAddon = new PrintItemAddon(
                        item,
                        addon.getId(),
                        addon.getPrice()
                );

                printItemAddonRepository.save(itemAddon);
            }

            orderTotal = orderTotal.add(itemTotal);
        }

        if (request.promotionCode() != null
                && !request.promotionCode().isBlank()) {

            Promotion promotion =
                    promotionService.findValidByCode(
                            request.promotionCode()
                    );

            BigDecimal discount =
                    calculateDiscount(promotion, orderTotal);

            orderTotal = orderTotal.subtract(discount);

            if (orderTotal.compareTo(BigDecimal.ZERO) < 0) {
                orderTotal = BigDecimal.ZERO;
            }

            OrderPromotion orderPromotion = new OrderPromotion(
                    order,
                    promotion.getId(),
                    discount
            );

            orderPromotionRepository.save(orderPromotion);
        }

        order.setTotalPrice(orderTotal);
        orderRepository.save(order);

        Map<Long, List<Long>> itemAddonIds = new HashMap<>();

        for (PrintItem item : items) {
            List<Long> addonIds = printItemAddonRepository
                    .findByItemId(item.getId())
                    .stream()
                    .map(PrintItemAddon::getAddonId)
                    .toList();

            itemAddonIds.put(item.getId(), addonIds);
        }

        return orderMapper.toResponse(
                order,
                items,
                itemAddonIds
        );
    }

    @Override
    public void deleteOrder(Long id) {

        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Order not found: " + id
            );
        }

        orderRepository.deleteById(id);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    private BigDecimal calculateDiscount(
            Promotion promotion,
            BigDecimal orderTotal
    ) {

        if (promotion == null) {
            return BigDecimal.ZERO;
        }

        if (promotion.getMinOrderAmount() != null
                && orderTotal.compareTo(
                        promotion.getMinOrderAmount()
                ) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountValue =
                promotion.getDiscountValue();

        if (discountValue == null
                || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return switch (promotion.getDiscountType()) {

            case PERCENTAGE -> orderTotal
                    .multiply(discountValue)
                    .divide(
                            BigDecimal.valueOf(100),
                            2,
                            RoundingMode.HALF_UP
                    );

            case FIXED_AMOUNT -> discountValue.min(orderTotal);
        };
    }
}

