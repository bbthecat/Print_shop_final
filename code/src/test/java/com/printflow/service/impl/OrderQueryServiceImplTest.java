package com.printflow.service.impl;

import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.mapper.OrderMapper;
import com.printflow.repository.AddonServiceRepository;
import com.printflow.repository.OrderRepository;
import com.printflow.repository.PrintItemAddonRepository;
import com.printflow.repository.PrintItemRepository;
import com.printflow.repository.PrintServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PrintItemRepository printItemRepository;

    @Mock
    private PrintItemAddonRepository printItemAddonRepository;

    @Mock
    private PrintServiceRepository printServiceRepository;

    @Mock
    private AddonServiceRepository addonServiceRepository;

    private OrderQueryServiceImpl orderQueryService;

    @BeforeEach
    void setUp() {
        orderQueryService = new OrderQueryServiceImpl(orderRepository, printItemRepository,
                printItemAddonRepository, printServiceRepository, addonServiceRepository, new OrderMapper());
        // order ของลูกค้า id 7
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(new PrintOrder("ORD-1", 7L, OrderStatus.PENDING, BigDecimal.TEN)));
    }

    private void givenNoItems() {
        when(printItemRepository.findByOrderId(any())).thenReturn(List.of());
    }

    @Test
    void getByIdForUser_ownOrder_isAllowed() {
        givenNoItems();

        assertDoesNotThrow(() -> orderQueryService.getByIdForUser(1L, 7L, false));
    }

    @Test
    void getByIdForUser_otherCustomersOrder_throwsAccessDenied() {
        assertThrows(AccessDeniedException.class, () -> orderQueryService.getByIdForUser(1L, 8L, false));
    }

    @Test
    void getByIdForUser_staff_canViewAnyOrder() {
        givenNoItems();

        assertDoesNotThrow(() -> orderQueryService.getByIdForUser(1L, 99L, true));
    }
}
