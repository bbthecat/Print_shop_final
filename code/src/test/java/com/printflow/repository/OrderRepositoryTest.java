package com.printflow.repository;

import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.entity.User;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.domain.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("password");
        user.setRole(Role.CUSTOMER);
        user.setActive(true);

        return userRepository.save(user);
    }

    @Test
    void shouldSaveAndFindOrderById() {
        User user = createUser(
                "testuser1",
                "test1@example.com"
        );

        PrintOrder order = new PrintOrder(
                "TEST-001",
                user.getId(),
                OrderStatus.PENDING,
                BigDecimal.valueOf(100)
        );

        PrintOrder saved = orderRepository.save(order);

        var result = orderRepository.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals("TEST-001", result.get().getOrderNumber());
        assertEquals(user.getId(), result.get().getUserId());
        assertEquals(OrderStatus.PENDING, result.get().getStatus());
    }

    @Test
    void shouldFindOrderByOrderNumber() {
        User user = createUser(
                "testuser2",
                "test2@example.com"
        );

        PrintOrder order = new PrintOrder(
                "TEST-002",
                user.getId(),
                OrderStatus.CONFIRMED,
                BigDecimal.valueOf(200)
        );

        orderRepository.save(order);

        var result =
                orderRepository.findByOrderNumber("TEST-002");

        assertTrue(result.isPresent());
        assertEquals(
                user.getId(),
                result.get().getUserId()
        );
    }

    @Test
    void shouldFindOrdersByUserId() {
        User user = createUser(
                "testuser3",
                "test3@example.com"
        );

        orderRepository.save(new PrintOrder(
                "TEST-003",
                user.getId(),
                OrderStatus.PENDING,
                BigDecimal.valueOf(100)
        ));

        orderRepository.save(new PrintOrder(
                "TEST-004",
                user.getId(),
                OrderStatus.CONFIRMED,
                BigDecimal.valueOf(200)
        ));

        Page<PrintOrder> result =
                orderRepository.findByUserId(
                        user.getId(),
                        PageRequest.of(0, 10)
                );

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldFindOrdersByStatus() {
        User user1 = createUser(
                "testuser4",
                "test4@example.com"
        );

        User user2 = createUser(
                "testuser5",
                "test5@example.com"
        );

        orderRepository.save(new PrintOrder(
                "TEST-006",
                user1.getId(),
                OrderStatus.PENDING,
                BigDecimal.valueOf(100)
        ));

        orderRepository.save(new PrintOrder(
                "TEST-007",
                user2.getId(),
                OrderStatus.PENDING,
                BigDecimal.valueOf(200)
        ));

        orderRepository.save(new PrintOrder(
                "TEST-008",
                user1.getId(),
                OrderStatus.COMPLETED,
                BigDecimal.valueOf(300)
        ));

        Page<PrintOrder> result =
                orderRepository.findByStatus(
                        OrderStatus.PENDING,
                        PageRequest.of(0, 10)
                );

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldFindOrdersByUserIdAndStatus() {
        User user = createUser(
                "testuser6",
                "test6@example.com"
        );

        orderRepository.save(new PrintOrder(
                "TEST-009",
                user.getId(),
                OrderStatus.PENDING,
                BigDecimal.valueOf(100)
        ));

        orderRepository.save(new PrintOrder(
                "TEST-010",
                user.getId(),
                OrderStatus.COMPLETED,
                BigDecimal.valueOf(200)
        ));

        Page<PrintOrder> result =
                orderRepository.findByUserIdAndStatus(
                        user.getId(),
                        OrderStatus.PENDING,
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                "TEST-009",
                result.getContent()
                        .get(0)
                        .getOrderNumber()
        );
    }
}