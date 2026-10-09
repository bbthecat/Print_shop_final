package com.printflow.service;

import com.printflow.domain.entity.AddonService;
import com.printflow.domain.entity.Payment;
import com.printflow.domain.entity.PrintService;
import com.printflow.domain.entity.User;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.domain.enums.PaymentMethod;
import com.printflow.domain.enums.PaymentStatus;
import com.printflow.domain.enums.PricingType;
import com.printflow.domain.enums.Role;
import com.printflow.dto.request.OrderCreateRequest;
import com.printflow.dto.request.OrderItemRequest;
import com.printflow.dto.response.OrderResponse;
import com.printflow.repository.AddonServiceRepository;
import com.printflow.repository.NotificationRepository;
import com.printflow.repository.OrderStatusHistoryRepository;
import com.printflow.repository.PaymentRepository;
import com.printflow.repository.PrintServiceRepository;
import com.printflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ทดสอบ Observer ของจริงทั้งระบบ (Spring event + listener + ฐานข้อมูล H2)
 * สร้าง order → เปลี่ยนสถานะ → ชำระเงิน → ยกเลิก แล้วดูว่า listener ทุกตัวทำงานครบ
 */
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class ObserverIntegrationTest {

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private OrderStatusService orderStatusService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PrintServiceRepository printServiceRepository;

    @Autowired
    private AddonServiceRepository addonServiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private OrderStatusHistoryRepository historyRepository;

    private Long customerId;
    private Long staffId;
    private Long serviceId;
    private Long stapleId;

    @BeforeEach
    void setUp() {
        customerId = saveUser("it-customer", Role.CUSTOMER);
        staffId = saveUser("it-staff", Role.STAFF);

        PrintService service = new PrintService();
        service.setName("IT B&W");
        service.setBasePrice(new BigDecimal("1.50"));
        service.setPricingType(PricingType.BLACK_WHITE);
        serviceId = printServiceRepository.save(service).getId();

        AddonService staple = new AddonService();
        staple.setName("IT Staple");
        staple.setPrice(new BigDecimal("2.00"));
        stapleId = addonServiceRepository.save(staple).getId();
    }

    private Long saveUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("password");
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user).getId();
    }

    private long unreadOf(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Test
    @DisplayName("ทั้ง flow: สร้าง → ยืนยัน → จ่าย → ยกเลิก แล้ว listener ทุกตัวทำงานครบ")
    void fullOrderFlowTriggersAllObservers() {
        OrderResponse order = orderCommandService.createOrder(customerId, new OrderCreateRequest(
                List.of(new OrderItemRequest(serviceId, 20, 1, List.of(stapleId))), null, "report.pdf", null));

        // ราคา: 1.50 x 20 หน้า + เย็บมุม 2.00 x 1 ชุด = 32.00
        assertEquals(0, new BigDecimal("32.00").compareTo(order.totalPrice()));

        // OrderCreatedEvent → PaymentCreationListener สร้าง payment UNPAID จากยอด order
        Payment payment = paymentRepository.findByOrderId(order.id()).orElseThrow();
        assertEquals(PaymentStatus.UNPAID, payment.getPaymentStatus());
        assertEquals(0, new BigDecimal("32.00").compareTo(payment.getAmount()));

        // OrderCreatedEvent → StaffNewOrderListener แจ้งเตือน STAFF
        assertEquals(1, unreadOf(staffId));

        // OrderStatusChangedEvent → OrderHistoryListener + InAppNotificationListener
        orderStatusService.changeStatus(order.id(), OrderStatus.CONFIRMED, staffId);
        assertEquals(1, historyRepository.findByOrderIdOrderByChangedAtAsc(order.id()).size());
        assertEquals(1, unreadOf(customerId));

        // จ่ายเงินแล้ว → ยอดขายใน Report นับ
        paymentService.markAsPaid(order.id(), PaymentMethod.CASH);
        LocalDate today = LocalDate.now();
        assertEquals(0, new BigDecimal("32.00").compareTo(reportService.getSummary(today, today).totalSales()));

        // ยกเลิก → PaymentRefundListener เปลี่ยนเป็น REFUNDED → Report ไม่นับแล้ว
        orderStatusService.changeStatus(order.id(), OrderStatus.CANCELLED, staffId);
        assertEquals(PaymentStatus.REFUNDED, paymentRepository.findByOrderId(order.id()).orElseThrow().getPaymentStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(reportService.getSummary(today, today).totalSales()));
        assertEquals(2, historyRepository.findByOrderIdOrderByChangedAtAsc(order.id()).size());
        assertEquals(2, unreadOf(customerId));
    }
}
