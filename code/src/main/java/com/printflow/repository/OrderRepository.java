package com.printflow.repository;

import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<PrintOrder, Long> {

    Optional<PrintOrder> findByOrderNumber(String orderNumber);

    Page<PrintOrder> findByUserId(Long userId, Pageable pageable);

    Page<PrintOrder> findByStatus(OrderStatus status, Pageable pageable);

    long countByStatus(OrderStatus status);

    Page<PrintOrder> findByUserIdAndStatus(
            Long userId,
            OrderStatus status,
            Pageable pageable
    );
}