package com.printflow.repository;

import com.printflow.domain.entity.OrderPromotion;
import com.printflow.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderPromotionRepository
        extends JpaRepository<OrderPromotion, Long> {

    List<OrderPromotion> findByOrderId(Long orderId);

    // ลูกค้าคนนี้เคยใช้โค้ดนี้ใน order ที่ยังไม่ถูกยกเลิกหรือยัง (ใช้ได้คนละ 1 ครั้งต่อโค้ด)
    boolean existsByPromotionIdAndOrder_UserIdAndOrder_StatusNot(Long promotionId, Long userId, OrderStatus status);

    // โค้ดทั้งหมดที่ลูกค้าคนนี้ใช้ไปแล้ว (ไม่นับ order ที่ยกเลิก)
    @Query("select distinct op.promotionId from OrderPromotion op "
            + "where op.order.userId = :userId and op.order.status <> :excluded")
    List<Long> findUsedPromotionIds(@Param("userId") Long userId, @Param("excluded") OrderStatus excluded);

    long countByPromotionId(Long promotionId);
}
