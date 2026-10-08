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
import com.printflow.exception.ValidationException;
import com.printflow.mapper.OrderMapper;
import com.printflow.repository.OrderPromotionRepository;
import com.printflow.repository.OrderRepository;
import com.printflow.repository.PrintItemAddonRepository;
import com.printflow.repository.PrintItemRepository;
import com.printflow.service.OrderCommandService;
import com.printflow.service.PromotionService;
import com.printflow.service.ServiceCatalogQueryService;
import com.printflow.service.strategy.discount.DiscountStrategyResolver;
import com.printflow.service.strategy.pricing.PricingCalculator;
import com.printflow.validation.OrderValidationContext;
import com.printflow.validation.OrderValidationHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final DiscountStrategyResolver discountStrategyResolver;
    private final OrderMapper orderMapper;
    private final OrderValidationHandler orderValidationChain;

    public OrderCommandServiceImpl(
            OrderRepository orderRepository,
            PrintItemRepository printItemRepository,
            PrintItemAddonRepository printItemAddonRepository,
            OrderPromotionRepository orderPromotionRepository,
            ServiceCatalogQueryService serviceCatalogQueryService,
            PromotionService promotionService,
            PricingCalculator pricingCalculator,
            DiscountStrategyResolver discountStrategyResolver,
            OrderMapper orderMapper,
            OrderValidationHandler orderValidationChain
    ) {
        this.orderRepository = orderRepository;
        this.printItemRepository = printItemRepository;
        this.printItemAddonRepository = printItemAddonRepository;
        this.orderPromotionRepository = orderPromotionRepository;
        this.serviceCatalogQueryService = serviceCatalogQueryService;
        this.promotionService = promotionService;
        this.pricingCalculator = pricingCalculator;
        this.discountStrategyResolver = discountStrategyResolver;
        this.orderMapper = orderMapper;
        this.orderValidationChain = orderValidationChain;
    }

    @Override
    public OrderResponse createOrder(OrderCreateRequest request) {

        PrintOrder order = new PrintOrder(
                generateOrderNumber(),
                request.userId(),
                OrderStatus.PENDING,
                BigDecimal.ZERO
        );

        List<PrintItem> itemsToValidate = new ArrayList<>();
        if (request.items() != null) {
            for (OrderItemRequest itemRequest : request.items()) {
                itemsToValidate.add(new PrintItem(
                        order,
                        itemRequest.serviceId(),
                        itemRequest.quantity(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ));
            }
        }

        List<OrderPromotion> promotionsToValidate = new ArrayList<>();
        if (request.promotionCode() != null && !request.promotionCode().isBlank()) {
            Promotion promotion = promotionService.findValidByCode(request.promotionCode());
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

            int pageCount = Math.max(1, itemRequest.pageCount());
            int copyCount = Math.max(1, itemRequest.quantity());

            BigDecimal itemTotal =
                    pricingCalculator.calculateItemTotal(
                            printService.getPricingType(),
                            printService.getBasePrice(),
                            pageCount,
                            copyCount,
                            addonPrices
                    );

            // unitPrice = ราคาพิมพ์ต่อแผ่น (basePrice เท่านั้น ไม่รวม addon)
            BigDecimal unitPrice = pricingCalculator.calculatePrintPrice(
                    printService.getPricingType(),
                    printService.getBasePrice(),
                    pageCount,
                    1
            );

            PrintItem item = new PrintItem(
                    order,
                    printService.getId(),
                    itemRequest.quantity(),
                    pageCount,
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

            // 🟡 ตรวจสอบยอดขั้นต่ำก่อน — แจ้งลูกค้าแทนการลด 0 แบบเงียบๆ
            if (promotion.getMinOrderAmount() != null
                    && orderTotal.compareTo(promotion.getMinOrderAmount()) < 0) {
                throw new ValidationException(
                        "โปรโมชัน '" + promotion.getCode() + "' ต้องสั่งซื้อขั้นต่ำ "
                        + promotion.getMinOrderAmount().toPlainString() + " บาท "
                        + "(ยอดปัจจุบัน " + orderTotal.toPlainString() + " บาท)"
                );
            }

            // 🟠 ใช้ DiscountStrategy จาก resolver แทน calculateDiscount() เดิม
            BigDecimal discount =
                    discountStrategyResolver
                            .resolve(promotion.getDiscountType())
                            .calculate(orderTotal, promotion);

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

    // calculateDiscount() ถูกลบออกแล้ว — ใช้ DiscountStrategyResolver แทน (บรรทัดประมาณ 200)
}

