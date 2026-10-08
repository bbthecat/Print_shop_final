package com.printflow.repository;

import com.printflow.domain.entity.OrderPromotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderPromotionRepository
        extends JpaRepository<OrderPromotion, Long> {

    List<OrderPromotion> findByOrderId(Long orderId);
}