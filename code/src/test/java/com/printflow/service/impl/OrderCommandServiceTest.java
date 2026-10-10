package com.printflow.service.impl;

import com.printflow.domain.entity.AddonService;
import com.printflow.domain.entity.OrderPromotion;
import com.printflow.domain.entity.PrintItem;
import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.entity.PrintService;
import com.printflow.domain.entity.Promotion;
import com.printflow.domain.enums.DiscountType;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.domain.enums.PricingType;
import com.printflow.dto.request.OrderCreateRequest;
import com.printflow.dto.request.OrderItemRequest;
import com.printflow.exception.InvalidStateTransitionException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.exception.ValidationException;
import com.printflow.repository.OrderFileRepository;
import com.printflow.repository.OrderPromotionRepository;
import com.printflow.repository.OrderRepository;
import com.printflow.repository.PrintItemAddonRepository;
import com.printflow.repository.PrintItemRepository;
import com.printflow.service.OrderQueryService;
import com.printflow.service.PromotionService;
import com.printflow.service.ServiceCatalogQueryService;
import com.printflow.service.event.OrderCreatedEvent;
import com.printflow.service.strategy.discount.DiscountStrategyResolver;
import com.printflow.service.strategy.discount.FixedAmountDiscountStrategy;
import com.printflow.service.strategy.discount.PercentageDiscountStrategy;
import com.printflow.service.strategy.pricing.BlackWhitePricingStrategy;
import com.printflow.service.strategy.pricing.ColorPricingStrategy;
import com.printflow.service.strategy.pricing.PhotoPricingStrategy;
import com.printflow.service.strategy.pricing.PricingCalculator;
import com.printflow.service.strategy.pricing.PricingStrategyResolver;
import com.printflow.validation.OrderValidationContext;
import com.printflow.validation.OrderValidationHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * ใช้ PricingCalculator และ DiscountStrategy ตัวจริง เพื่อตรวจสูตรราคาจริง
 * ส่วน repository / service อื่น ๆ ใช้ mock
 */
@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PrintItemRepository printItemRepository;

    @Mock
    private PrintItemAddonRepository printItemAddonRepository;

    @Mock
    private OrderFileRepository orderFileRepository;

    @Mock
    private OrderPromotionRepository orderPromotionRepository;

    @Mock
    private ServiceCatalogQueryService serviceCatalogQueryService;

    @Mock
    private PromotionService promotionService;

    @Mock
    private OrderQueryService orderQueryService;

    @Mock
    private OrderValidationHandler orderValidationChain;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private OrderCommandServiceImpl orderCommandService;

    @BeforeEach
    void setUp() {
        PricingCalculator pricingCalculator = new PricingCalculator(new PricingStrategyResolver(List.of(
                new BlackWhitePricingStrategy(), new ColorPricingStrategy(), new PhotoPricingStrategy())));
        DiscountStrategyResolver discountStrategyResolver = new DiscountStrategyResolver(List.of(
                new PercentageDiscountStrategy(), new FixedAmountDiscountStrategy()));

        orderCommandService = new OrderCommandServiceImpl(
                orderRepository,
                printItemRepository,
                printItemAddonRepository,
                orderFileRepository,
                orderPromotionRepository,
                serviceCatalogQueryService,
                promotionService,
                pricingCalculator,
                discountStrategyResolver,
                orderQueryService,
                orderValidationChain,
                eventPublisher
        );
    }

    private void givenBlackWhiteService() {
        PrintService service = new PrintService();
        service.setId(1L);
        service.setName("Document B&W (A4)");
        service.setBasePrice(new BigDecimal("1.50"));
        service.setPricingType(PricingType.BLACK_WHITE);
        when(serviceCatalogQueryService.findActivePrintServiceById(1L)).thenReturn(service);
    }

    private void givenAddon(Long id, String price) {
        AddonService addon = new AddonService();
        addon.setId(id);
        addon.setPrice(new BigDecimal(price));
        when(serviceCatalogQueryService.findActiveAddonServiceById(id)).thenReturn(addon);
    }

    private void givenRepositoriesSave() {
        when(orderRepository.save(any(PrintOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(printItemRepository.save(any(PrintItem.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Promotion promotion(DiscountType type, String value, String minOrder) {
        Promotion promotion = new Promotion();
        promotion.setId(10L);
        promotion.setCode("PROMO");
        promotion.setDiscountType(type);
        promotion.setDiscountValue(new BigDecimal(value));
        promotion.setMinOrderAmount(new BigDecimal(minOrder));
        return promotion;
    }

    private PrintItem savedItem() {
        ArgumentCaptor<PrintItem> captor = ArgumentCaptor.forClass(PrintItem.class);
        verify(printItemRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void createOrder_20PagesOneSetWithStaple_chargesStapleOnce() {
        givenBlackWhiteService();
        givenAddon(1L, "2.00");
        when(printItemRepository.save(any(PrintItem.class))).thenAnswer(inv -> inv.getArgument(0));

        PrintOrder[] saved = new PrintOrder[1];
        when(orderRepository.save(any(PrintOrder.class))).thenAnswer(inv -> saved[0] = inv.getArgument(0));

        orderCommandService.createOrder(7L, new OrderCreateRequest(
                List.of(new OrderItemRequest(1L, 20, 1, List.of(1L))), null, null, null));

        // 1.50 x 20 หน้า x 1 ชุด + เย็บมุม 2.00 x 1 ชุด = 32.00 (เดิมคิดผิดเป็น 70.00)
        PrintItem item = savedItem();
        assertEquals(20, item.getPageCount());
        assertEquals(1, item.getQuantity());
        assertEquals(0, new BigDecimal("32.00").compareTo(item.getSubtotal()));
        assertEquals(0, new BigDecimal("32.00").compareTo(saved[0].getTotalPrice()));
        assertEquals(7L, saved[0].getUserId());
        verify(eventPublisher).publishEvent(any(OrderCreatedEvent.class));
    }

    @Test
    void createOrder_10PagesTwoSetsWithTwoAddons_chargesAddonsPerSet() {
        givenBlackWhiteService();
        givenAddon(1L, "2.00");
        givenAddon(2L, "25.00");
        givenRepositoriesSave();

        orderCommandService.createOrder(7L, new OrderCreateRequest(
                List.of(new OrderItemRequest(1L, 10, 2, List.of(1L, 2L))), null, null, null));

        // 1.50 x 10 x 2 = 30.00 + (2 + 25) x 2 ชุด = 54.00 → 84.00, ราคาต่อชุด 42.00
        PrintItem item = savedItem();
        assertEquals(0, new BigDecimal("84.00").compareTo(item.getSubtotal()));
        assertEquals(0, new BigDecimal("42.00").compareTo(item.getUnitPrice()));
    }

    @Test
    void createOrder_duplicateAddonIds_chargesAddonOnce() {
        givenBlackWhiteService();
        givenAddon(1L, "2.00");
        givenRepositoriesSave();

        orderCommandService.createOrder(7L, new OrderCreateRequest(
                List.of(new OrderItemRequest(1L, 20, 1, List.of(1L, 1L))), null, null, null));

        // ส่ง id เย็บมุมซ้ำมา ต้องคิดแค่ครั้งเดียว: 30.00 + 2.00 = 32.00
        assertEquals(0, new BigDecimal("32.00").compareTo(savedItem().getSubtotal()));
        verify(printItemAddonRepository, times(1)).save(any());
    }

    @Test
    void createOrder_withoutItems_throwsValidationException() {
        OrderCreateRequest request = new OrderCreateRequest(List.of(), null, null, null);

        assertThrows(ValidationException.class, () -> orderCommandService.createOrder(7L, request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_withPercentagePromotion_usesDiscountStrategy() {
        givenBlackWhiteService();
        givenRepositoriesSave();
        when(promotionService.findValidByCode("PROMO"))
                .thenReturn(promotion(DiscountType.PERCENTAGE, "10", "0"));

        orderCommandService.createOrder(7L, new OrderCreateRequest(
                List.of(new OrderItemRequest(1L, 100, 1, List.of())), "PROMO", null, null));

        // 1.50 x 100 = 150.00, ลด 10% = 15.00
        ArgumentCaptor<OrderPromotion> captor = ArgumentCaptor.forClass(OrderPromotion.class);
        verify(orderPromotionRepository).save(captor.capture());
        assertEquals(0, new BigDecimal("15.00").compareTo(captor.getValue().getDiscountAmount()));
    }

    @Test
    void createOrder_belowPromotionMinimum_throwsValidationException() {
        givenBlackWhiteService();
        givenRepositoriesSave();
        when(promotionService.findValidByCode("PROMO"))
                .thenReturn(promotion(DiscountType.FIXED_AMOUNT, "30", "200"));

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(1L, 20, 1, List.of())), "PROMO", null, null);

        assertThrows(ValidationException.class, () -> orderCommandService.createOrder(7L, request));
        verify(orderPromotionRepository, never()).save(any());
    }

    @Test
    void createOrder_withFileName_passesFileToValidationChainAndSavesIt() {
        givenBlackWhiteService();
        givenRepositoriesSave();

        orderCommandService.createOrder(7L, new OrderCreateRequest(
                List.of(new OrderItemRequest(1L, 1, 1, List.of())), null, "report.PDF", null));

        // ชนิดไฟล์ถูกเดาจากนามสกุล แล้วส่งให้ FileTypeValidationHandler ตรวจใน chain
        ArgumentCaptor<OrderValidationContext> context = ArgumentCaptor.forClass(OrderValidationContext.class);
        verify(orderValidationChain).handle(context.capture());
        assertEquals("application/pdf", context.getValue().getFiles().get(0).getFileType());
        assertEquals("นำไฟล์มาที่ร้าน", context.getValue().getFiles().get(0).getFilePath());
        verify(orderFileRepository).saveAll(any());
    }

    @Test
    void createOrder_validationFails_savesNothing() {
        doThrow(new ValidationException("Quantity must be greater than 0"))
                .when(orderValidationChain).handle(any(OrderValidationContext.class));

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(1L, 1, 0, List.of())), null, null, null);

        assertThrows(ValidationException.class, () -> orderCommandService.createOrder(7L, request));
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void deleteOrder_notFound_throwsResourceNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderCommandService.deleteOrder(999L));
    }

    @Test
    void deleteOrder_pending_deletes() {
        PrintOrder order = new PrintOrder("ORD-1", 7L, OrderStatus.PENDING, BigDecimal.TEN);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderCommandService.deleteOrder(1L);

        verify(orderRepository).delete(order);
    }

    @Test
    void deleteOrder_alreadyStarted_throwsAndKeepsHistory() {
        PrintOrder order = new PrintOrder("ORD-1", 7L, OrderStatus.PROCESSING, BigDecimal.TEN);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidStateTransitionException.class, () -> orderCommandService.deleteOrder(1L));
        verify(orderRepository, never()).delete(any());
    }
}
