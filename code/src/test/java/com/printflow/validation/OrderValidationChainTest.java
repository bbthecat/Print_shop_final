package com.printflow.validation;

import com.printflow.domain.entity.PrintItem;
import com.printflow.domain.entity.PrintOrder;
import com.printflow.exception.ValidationException;
import com.printflow.service.ServiceCatalogQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderValidationChainTest {

    private final ServiceCatalogQueryService serviceCatalogQueryService =
            Mockito.mock(ServiceCatalogQueryService.class);

    private OrderValidationHandler createChain() {
        OrderValidationHandler serviceHandler =
                new ServiceAvailabilityHandler(serviceCatalogQueryService);

        OrderValidationHandler quantityHandler =
                new QuantityValidationHandler();

        serviceHandler.setNext(quantityHandler);

        return serviceHandler;
    }

    private OrderValidationContext createContext(Integer quantity) {
        PrintOrder order = new PrintOrder(
                "ORD-TEST",
                1L,
                com.printflow.domain.enums.OrderStatus.PENDING,
                BigDecimal.ZERO
        );

        PrintItem item = new PrintItem(
                order,
                1L,
                quantity,
                BigDecimal.TEN,
                BigDecimal.TEN
        );

        return new OrderValidationContext(
                order,
                List.of(item),
                List.of(),
                List.of()
        );
    }

    @Test
    void shouldPassWhenQuantityIsValid() {
        Mockito.when(
                serviceCatalogQueryService.findActivePrintServiceById(1L)
        ).thenReturn(null);

        assertDoesNotThrow(() ->
                createChain().handle(createContext(2))
        );
    }

    @Test
    void shouldRejectWhenQuantityIsZero() {
        Mockito.when(
                serviceCatalogQueryService.findActivePrintServiceById(1L)
        ).thenReturn(null);

        assertThrows(
                ValidationException.class,
                () -> createChain().handle(createContext(0))
        );
    }

    @Test
    void shouldRejectWhenQuantityIsNegative() {
        Mockito.when(
                serviceCatalogQueryService.findActivePrintServiceById(1L)
        ).thenReturn(null);

        assertThrows(
                ValidationException.class,
                () -> createChain().handle(createContext(-1))
        );
    }

    @Test
    void shouldRejectWhenServiceIsUnavailable() {
        Mockito.when(
                serviceCatalogQueryService.findActivePrintServiceById(1L)
        ).thenThrow(new ValidationException("Service is not available"));

        assertThrows(
                ValidationException.class,
                () -> createChain().handle(createContext(2))
        );
    }
}