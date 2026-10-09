package com.printflow.validation;

import com.printflow.domain.entity.OrderFile;
import com.printflow.domain.entity.OrderPromotion;
import com.printflow.domain.entity.PrintItem;
import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.entity.Promotion;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.exception.ValidationException;
import com.printflow.repository.OrderPromotionRepository;
import com.printflow.repository.PromotionRepository;
import com.printflow.service.ServiceCatalogQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderValidationChainTest {

    private final ServiceCatalogQueryService serviceCatalogQueryService =
            Mockito.mock(ServiceCatalogQueryService.class);
    private final PromotionRepository promotionRepository =
            Mockito.mock(PromotionRepository.class);
    private final OrderPromotionRepository orderPromotionRepository =
            Mockito.mock(OrderPromotionRepository.class);

    private OrderValidationHandler createChain() {
        OrderValidationHandler serviceHandler =
                new ServiceAvailabilityHandler(serviceCatalogQueryService);

        OrderValidationHandler fileTypeHandler =
                new FileTypeValidationHandler();

        OrderValidationHandler quantityHandler =
                new QuantityValidationHandler();

        OrderValidationHandler promotionHandler =
                new PromotionValidityHandler(promotionRepository);

        OrderValidationHandler usageLimitHandler =
                new PromotionUsageLimitHandler(orderPromotionRepository, promotionRepository);

        serviceHandler
                .setNext(fileTypeHandler)
                .setNext(quantityHandler)
                .setNext(promotionHandler)
                .setNext(usageLimitHandler);

        return serviceHandler;
    }

    private OrderValidationContext createContext(
            Integer quantity,
            List<OrderFile> files,
            List<OrderPromotion> promotions
    ) {
        PrintOrder order = new PrintOrder(
                "ORD-TEST",
                1L,
                OrderStatus.PENDING,
                BigDecimal.ZERO
        );

        PrintItem item = new PrintItem(
                order,
                1L,
                1,
                quantity,
                BigDecimal.TEN,
                BigDecimal.TEN
        );

        return new OrderValidationContext(
                order,
                List.of(item),
                files != null ? files : List.of(),
                promotions != null ? promotions : List.of()
        );
    }

    @Test
    void shouldPassWhenAllValidationsSucceed() {
        Mockito.when(
                serviceCatalogQueryService.findActivePrintServiceById(1L)
        ).thenReturn(null);

        assertDoesNotThrow(() ->
                createChain().handle(createContext(2, List.of(), List.of()))
        );
    }

    @Test
    void shouldRejectWhenQuantityIsZero() {
        Mockito.when(
                serviceCatalogQueryService.findActivePrintServiceById(1L)
        ).thenReturn(null);

        assertThrows(
                ValidationException.class,
                () -> createChain().handle(createContext(0, List.of(), List.of()))
        );
    }

    @Test
    void shouldRejectWhenQuantityIsNegative() {
        Mockito.when(
                serviceCatalogQueryService.findActivePrintServiceById(1L)
        ).thenReturn(null);

        assertThrows(
                ValidationException.class,
                () -> createChain().handle(createContext(-1, List.of(), List.of()))
        );
    }

    @Test
    void shouldRejectWhenServiceIsUnavailable() {
        Mockito.when(
                serviceCatalogQueryService.findActivePrintServiceById(1L)
        ).thenThrow(new ValidationException("Service is not available"));

        assertThrows(
                ValidationException.class,
                () -> createChain().handle(createContext(2, List.of(), List.of()))
        );
    }

    @Test
    void shouldRejectWhenFileTypeIsNotSupported() {
        Mockito.when(
                serviceCatalogQueryService.findActivePrintServiceById(1L)
        ).thenReturn(null);

        PrintOrder order = new PrintOrder("ORD-TEST", 1L, OrderStatus.PENDING, BigDecimal.ZERO);
        OrderFile file = new OrderFile(order, "app.exe", "/uploads/app.exe", "application/x-msdownload", 1024L);

        assertThrows(
                ValidationException.class,
                () -> createChain().handle(createContext(2, List.of(file), List.of()))
        );
    }

    @Test
    void shouldPassWhenFileTypeIsSupported() {
        Mockito.when(
                serviceCatalogQueryService.findActivePrintServiceById(1L)
        ).thenReturn(null);

        PrintOrder order = new PrintOrder("ORD-TEST", 1L, OrderStatus.PENDING, BigDecimal.ZERO);
        OrderFile pdf = new OrderFile(order, "doc.pdf", "/uploads/doc.pdf", "application/pdf", 1024L);

        assertDoesNotThrow(() ->
                createChain().handle(createContext(2, List.of(pdf), List.of()))
        );
    }

    @Test
    void shouldRejectWhenPromotionIsInactive() {
        Mockito.when(
                serviceCatalogQueryService.findActivePrintServiceById(1L)
        ).thenReturn(null);

        PrintOrder order = new PrintOrder("ORD-TEST", 1L, OrderStatus.PENDING, BigDecimal.ZERO);
        OrderPromotion orderPromo = new OrderPromotion(order, 10L, BigDecimal.TEN);

        Promotion promo = new Promotion();
        promo.setCode("INACTIVE");
        promo.setActive(false);

        Mockito.when(promotionRepository.findById(10L))
                .thenReturn(Optional.of(promo));

        assertThrows(
                ValidationException.class,
                () -> createChain().handle(createContext(2, List.of(), List.of(orderPromo)))
        );
    }

    @Test
    void shouldRejectWhenPromotionIsExpired() {
        Mockito.when(
                serviceCatalogQueryService.findActivePrintServiceById(1L)
        ).thenReturn(null);

        PrintOrder order = new PrintOrder("ORD-TEST", 1L, OrderStatus.PENDING, BigDecimal.ZERO);
        OrderPromotion orderPromo = new OrderPromotion(order, 10L, BigDecimal.TEN);

        Promotion promo = new Promotion();
        promo.setCode("EXPIRED");
        promo.setActive(true);
        promo.setStartDate(LocalDateTime.now().minusDays(10));
        promo.setEndDate(LocalDateTime.now().minusDays(1));

        Mockito.when(promotionRepository.findById(10L))
                .thenReturn(Optional.of(promo));

        assertThrows(
                ValidationException.class,
                () -> createChain().handle(createContext(2, List.of(), List.of(orderPromo)))
        );
    }

    private OrderPromotion validPromotion(PrintOrder order) {
        Promotion promo = new Promotion();
        promo.setCode("SAVE10");
        promo.setActive(true);
        promo.setStartDate(LocalDateTime.now().minusDays(1));
        promo.setEndDate(LocalDateTime.now().plusDays(1));
        Mockito.when(promotionRepository.findById(10L)).thenReturn(Optional.of(promo));
        return new OrderPromotion(order, 10L, BigDecimal.TEN);
    }

    @Test
    void shouldPassWhenCustomerHasNotUsedThePromotionYet() {
        PrintOrder order = new PrintOrder("ORD-TEST", 1L, OrderStatus.PENDING, BigDecimal.ZERO);
        OrderPromotion orderPromo = validPromotion(order);
        Mockito.when(orderPromotionRepository.existsByPromotionIdAndOrder_UserIdAndOrder_StatusNot(
                10L, 1L, OrderStatus.CANCELLED)).thenReturn(false);

        assertDoesNotThrow(() -> createChain().handle(createContext(2, List.of(), List.of(orderPromo))));
    }

    @Test
    void shouldRejectWhenCustomerAlreadyUsedThePromotion() {
        PrintOrder order = new PrintOrder("ORD-TEST", 1L, OrderStatus.PENDING, BigDecimal.ZERO);
        OrderPromotion orderPromo = validPromotion(order);
        // นับเฉพาะ order ที่ไม่ได้ถูกยกเลิก
        Mockito.when(orderPromotionRepository.existsByPromotionIdAndOrder_UserIdAndOrder_StatusNot(
                10L, 1L, OrderStatus.CANCELLED)).thenReturn(true);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> createChain().handle(createContext(2, List.of(), List.of(orderPromo)))
        );
        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("SAVE10"));
    }
}
