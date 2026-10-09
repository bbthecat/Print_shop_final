package com.printflow.repository;

import com.printflow.domain.entity.Payment;
import com.printflow.domain.entity.PrintItem;
import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.entity.PrintService;
import com.printflow.domain.entity.User;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.domain.enums.PaymentStatus;
import com.printflow.domain.enums.PricingType;
import com.printflow.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private TestEntityManager em;

    private final LocalDateTime today = LocalDate.now().atStartOfDay();
    private final LocalDateTime tomorrow = today.plusDays(1);

    private Long userId;
    private PrintService blackWhite;
    private PrintService color;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("report-user");
        user.setEmail("report@example.com");
        user.setPasswordHash("password");
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        userId = em.persist(user).getId();

        blackWhite = em.persist(service("ถ่ายเอกสารขาวดำ", PricingType.BLACK_WHITE));
        color = em.persist(service("พิมพ์สี", PricingType.COLOR));
    }

    private PrintService service(String name, PricingType type) {
        PrintService service = new PrintService();
        service.setName(name);
        service.setBasePrice(BigDecimal.ONE);
        service.setPricingType(type);
        return service;
    }

    private PrintOrder order(String number, OrderStatus status, PrintService service, int quantity) {
        BigDecimal subtotal = BigDecimal.valueOf(quantity);
        PrintOrder order = em.persist(new PrintOrder(number, userId, status, subtotal));
        em.persist(new PrintItem(order, service.getId(), 1, quantity, BigDecimal.ONE, subtotal));
        return order;
    }

    @Test
    void countOrdersByStatus_groupsOrdersCreatedInRange() {
        order("R-001", OrderStatus.PENDING, blackWhite, 10);
        order("R-002", OrderStatus.PENDING, color, 5);
        order("R-003", OrderStatus.COMPLETED, color, 1);
        em.flush();

        Map<OrderStatus, Long> counts = reportRepository.countOrdersByStatus(today, tomorrow).stream()
                .collect(Collectors.toMap(ReportRepository.StatusCount::getStatus, ReportRepository.StatusCount::getTotal));

        assertEquals(2L, counts.get(OrderStatus.PENDING));
        assertEquals(1L, counts.get(OrderStatus.COMPLETED));
    }

    @Test
    void countOrdersByStatus_outsideRange_returnsEmpty() {
        order("R-001", OrderStatus.PENDING, blackWhite, 10);
        em.flush();

        assertTrue(reportRepository.countOrdersByStatus(tomorrow, tomorrow.plusDays(1)).isEmpty());
    }

    @Test
    void sumPaymentAmount_countsOnlyPaidPayments() {
        PrintOrder paidOrder = order("R-001", OrderStatus.COMPLETED, blackWhite, 10);
        PrintOrder unpaidOrder = order("R-002", OrderStatus.PENDING, color, 5);

        Payment paid = new Payment(paidOrder.getId(), new BigDecimal("150.00"));
        paid.setPaymentStatus(PaymentStatus.PAID);
        paid.setPaidAt(LocalDateTime.now());
        em.persist(paid);
        em.persist(new Payment(unpaidOrder.getId(), new BigDecimal("80.00")));
        em.flush();

        BigDecimal total = reportRepository.sumPaymentAmount(PaymentStatus.PAID, today, tomorrow);

        assertEquals(0, new BigDecimal("150.00").compareTo(total));
    }

    @Test
    void sumPaymentAmount_noPayments_returnsNull() {
        assertNull(reportRepository.sumPaymentAmount(PaymentStatus.PAID, today, tomorrow));
    }

    @Test
    void findTopServices_sortsByQuantity_andSkipsCancelledOrders() {
        order("R-001", OrderStatus.PENDING, blackWhite, 10);
        order("R-002", OrderStatus.COMPLETED, color, 30);
        order("R-003", OrderStatus.CANCELLED, blackWhite, 100);
        order("R-004", OrderStatus.READY, blackWhite, 5);
        em.flush();

        List<ReportRepository.ServiceUsage> top = reportRepository.findTopServices(
                OrderStatus.CANCELLED, today, tomorrow, PageRequest.of(0, 5));

        assertEquals(2, top.size());
        assertEquals("พิมพ์สี", top.get(0).getServiceName());
        assertEquals(30L, top.get(0).getTotalQuantity());
        assertEquals("ถ่ายเอกสารขาวดำ", top.get(1).getServiceName());
        assertEquals(15L, top.get(1).getTotalQuantity());
        assertEquals(0, new BigDecimal("15").compareTo(top.get(1).getTotalSales()));
    }
}
