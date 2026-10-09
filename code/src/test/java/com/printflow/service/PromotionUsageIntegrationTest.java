package com.printflow.service;

import com.printflow.domain.entity.PrintService;
import com.printflow.domain.entity.Promotion;
import com.printflow.domain.entity.User;
import com.printflow.domain.enums.DiscountType;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.domain.enums.PricingType;
import com.printflow.domain.enums.Role;
import com.printflow.dto.request.OrderCreateRequest;
import com.printflow.dto.request.OrderItemRequest;
import com.printflow.dto.response.OrderResponse;
import com.printflow.exception.ValidationException;
import com.printflow.repository.PrintServiceRepository;
import com.printflow.repository.PromotionRepository;
import com.printflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * กฎโค้ดโปรโมชันกับฐานข้อมูลจริง (H2): ลูกค้า 1 คนใช้แต่ละโค้ดได้ 1 ครั้ง, order ที่ยกเลิกคืนสิทธิ์,
 * และลบโค้ดได้เฉพาะโค้ดที่ยังไม่มีคนใช้
 */
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class PromotionUsageIntegrationTest {

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private OrderStatusService orderStatusService;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PrintServiceRepository printServiceRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    private Long aliceId;
    private Long bobId;
    private Long serviceId;
    private Long save10Id;
    private Long save20Id;

    @BeforeEach
    void setUp() {
        aliceId = saveUser("promo-alice");
        bobId = saveUser("promo-bob");

        PrintService service = new PrintService();
        service.setName("Promo B&W");
        service.setBasePrice(new BigDecimal("1.50"));
        service.setPricingType(PricingType.BLACK_WHITE);
        serviceId = printServiceRepository.save(service).getId();

        save10Id = savePromotion("ITSAVE10");
        save20Id = savePromotion("ITSAVE20");
    }

    private Long saveUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("password");
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        return userRepository.save(user).getId();
    }

    private Long savePromotion(String code) {
        Promotion promo = new Promotion();
        promo.setCode(code);
        promo.setDescription(code);
        promo.setDiscountType(DiscountType.PERCENTAGE);
        promo.setDiscountValue(BigDecimal.TEN);
        promo.setMinOrderAmount(BigDecimal.ZERO);
        promo.setStartDate(LocalDateTime.now().minusDays(1));
        promo.setEndDate(LocalDateTime.now().plusDays(7));
        return promotionRepository.save(promo).getId();
    }

    private OrderResponse order(Long userId, String code) {
        return orderCommandService.createOrder(userId, new OrderCreateRequest(
                List.of(new OrderItemRequest(serviceId, 20, 1, List.of())), code, null, null));
    }

    @Test
    @DisplayName("ใช้แต่ละโค้ดได้คนละ 1 ครั้ง: โค้ดต่างกันใช้ได้, โค้ดเดิมซ้ำไม่ได้, คนอื่นยังใช้ได้")
    void eachCustomerCanUseEachCodeOnce() {
        OrderResponse first = order(aliceId, "ITSAVE10");
        assertEquals(0, new BigDecimal("27.00").compareTo(first.totalPrice()));
        assertEquals("ITSAVE10", first.promotionCode());

        ValidationException again = assertThrows(ValidationException.class, () -> order(aliceId, "ITSAVE10"));
        assertTrue(again.getMessage().contains("ITSAVE10"));

        assertDoesNotThrow(() -> order(aliceId, "ITSAVE20"));
        assertDoesNotThrow(() -> order(bobId, "ITSAVE10"));
        assertEquals(Set.of(save10Id, save20Id), orderQueryService.getUsedPromotionIds(aliceId));
    }

    @Test
    @DisplayName("order ที่ยกเลิกคืนสิทธิ์ใช้โค้ด")
    void cancelledOrderGivesTheCodeBack() {
        OrderResponse first = order(aliceId, "ITSAVE10");
        orderStatusService.cancelByCustomer(first.id(), aliceId);

        assertEquals(Set.of(), orderQueryService.getUsedPromotionIds(aliceId));
        assertEquals(OrderStatus.PENDING, order(aliceId, "ITSAVE10").status());
    }

    @Test
    @DisplayName("ลบได้เฉพาะโค้ดที่ยังไม่มีคนใช้")
    void deleteOnlyUnusedPromotion() {
        order(aliceId, "ITSAVE10");

        assertThrows(ValidationException.class, () -> promotionService.delete(save10Id));
        assertTrue(promotionRepository.findById(save10Id).isPresent());

        promotionService.delete(save20Id);
        assertTrue(promotionRepository.findById(save20Id).isEmpty());
    }
}
