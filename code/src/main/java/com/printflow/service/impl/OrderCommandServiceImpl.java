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
import com.printflow.exception.InvalidStateTransitionException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.exception.ValidationException;
import com.printflow.repository.OrderPromotionRepository;
import com.printflow.repository.OrderRepository;
import com.printflow.repository.PrintItemAddonRepository;
import com.printflow.repository.PrintItemRepository;
import com.printflow.service.OrderCommandService;
import com.printflow.service.OrderQueryService;
import com.printflow.service.PromotionService;
import com.printflow.service.ServiceCatalogQueryService;
import com.printflow.service.event.OrderCreatedEvent;
import com.printflow.service.strategy.discount.DiscountStrategyResolver;
import com.printflow.service.strategy.pricing.PricingCalculator;
import com.printflow.validation.OrderValidationContext;
import com.printflow.validation.OrderValidationHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

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
    private final DiscountStrategyResolver discountStrategyResolver;
    private final OrderQueryService orderQueryService;
    private final OrderValidationHandler orderValidationChain;
    private final ApplicationEventPublisher eventPublisher;

    public OrderCommandServiceImpl(
            OrderRepository orderRepository,
            PrintItemRepository printItemRepository,
            PrintItemAddonRepository printItemAddonRepository,
            OrderPromotionRepository orderPromotionRepository,
            ServiceCatalogQueryService serviceCatalogQueryService,
            PromotionService promotionService,
            PricingCalculator pricingCalculator,
            DiscountStrategyResolver discountStrategyResolver,
            OrderQueryService orderQueryService,
            OrderValidationHandler orderValidationChain,
            ApplicationEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.printItemRepository = printItemRepository;
        this.printItemAddonRepository = printItemAddonRepository;
        this.orderPromotionRepository = orderPromotionRepository;
        this.serviceCatalogQueryService = serviceCatalogQueryService;
        this.promotionService = promotionService;
        this.pricingCalculator = pricingCalculator;
        this.discountStrategyResolver = discountStrategyResolver;
        this.orderQueryService = orderQueryService;
        this.orderValidationChain = orderValidationChain;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public OrderResponse createOrder(Long userId, OrderCreateRequest request) {

        PrintOrder order = new PrintOrder(
                generateOrderNumber(),
                userId,
                OrderStatus.PENDING,
                BigDecimal.ZERO
        );

        List<PrintItem> itemsToValidate = new ArrayList<>();
        if (request.items() != null) {
            for (OrderItemRequest itemRequest : request.items()) {
                itemsToValidate.add(new PrintItem(
                        order,
                        itemRequest.serviceId(),
                        itemRequest.pageCount(),
                        itemRequest.quantity(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ));
            }
        }

        Promotion promotion = null;
        List<OrderPromotion> promotionsToValidate = new ArrayList<>();
        if (request.promotionCode() != null && !request.promotionCode().isBlank()) {
            promotion = promotionService.findValidByCode(request.promotionCode());
            promotionsToValidate.add(new OrderPromotion(
                    order,
                    promotion.getId(),
                    BigDecimal.ZERO
            ));
        }

        OrderValidationContext validationContext = new OrderValidationContext(
                order,
                itemsToValidate,
                List.of(),
                promotionsToValidate
        );

        orderValidationChain.handle(validationContext);

        order = orderRepository.save(order);

        BigDecimal orderTotal = BigDecimal.ZERO;

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

            // ราคา = ราคาพิมพ์ (ตาม Strategy ของประเภทงาน) + บริการเสริม x จำนวนชุด
            int pageCount = itemRequest.pageCount();
            int copyCount = itemRequest.quantity();

            BigDecimal itemTotal =
                    pricingCalculator.calculateItemTotal(
                            printService.getPricingType(),
                            printService.getBasePrice(),
                            pageCount,
                            copyCount,
                            addonPrices
                    );

            // ราคาต่อ 1 ชุด
            BigDecimal unitPrice = itemTotal.divide(
                    BigDecimal.valueOf(copyCount),
                    2,
                    RoundingMode.HALF_UP
            );

            PrintItem item = new PrintItem(
                    order,
                    printService.getId(),
                    pageCount,
                    copyCount,
                    unitPrice,
                    itemTotal
            );

            item = printItemRepository.save(item);

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

        if (promotion != null) {
            BigDecimal discount = calculateDiscount(promotion, orderTotal);
            orderTotal = orderTotal.subtract(discount);

            orderPromotionRepository.save(new OrderPromotion(
                    order,
                    promotion.getId(),
                    discount
            ));
        }

        order.setTotalPrice(orderTotal);
        orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCreatedEvent(order.getId()));

        return orderQueryService.getById(order.getId());
    }

    @Override
    public void deleteOrder(Long id) {

        PrintOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + id
                ));

        // ลบได้เฉพาะ order ที่ยังไม่เริ่มงาน (ยังไม่มีประวัติสถานะ)
        // order ที่เริ่มแล้วให้เปลี่ยนเป็น CANCELLED แทน เพื่อเก็บประวัติไว้
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidStateTransitionException(
                    "Only PENDING orders can be deleted. Cancel the order instead: " + id
            );
        }

        orderRepository.delete(order);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    // ใช้ Strategy ส่วนลดของ P2 (PERCENTAGE / FIXED_AMOUNT) แทนการเขียน if/switch เอง
    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal orderTotal) {
        if (promotion.getMinOrderAmount() != null
                && orderTotal.compareTo(promotion.getMinOrderAmount()) < 0) {
            throw new ValidationException(
                    "ยอดสั่งซื้อ " + orderTotal + " บาท ยังไม่ถึงขั้นต่ำของโปรโมชัน "
                            + promotion.getCode() + " (" + promotion.getMinOrderAmount() + " บาท)"
            );
        }

        return discountStrategyResolver
                .resolve(promotion.getDiscountType())
                .calculate(orderTotal, promotion);
    }
}
