package com.printflow.validation;

import com.printflow.domain.entity.OrderPromotion;
import com.printflow.domain.enums.OrderStatus;
import com.printflow.exception.ValidationException;
import com.printflow.repository.OrderPromotionRepository;
import com.printflow.repository.PromotionRepository;

/**
 * ลูกค้า 1 คนใช้โค้ดโปรโมชันแต่ละโค้ดได้ 1 ครั้ง (มี 5 โค้ด ก็ใช้ได้ 5 ครั้ง โค้ดละครั้ง)
 * order ที่ถูกยกเลิกไม่นับ ลูกค้าใช้โค้ดนั้นใหม่ได้
 */
public class PromotionUsageLimitHandler extends OrderValidationHandler {

    private final OrderPromotionRepository orderPromotionRepository;
    private final PromotionRepository promotionRepository;

    public PromotionUsageLimitHandler(
            OrderPromotionRepository orderPromotionRepository,
            PromotionRepository promotionRepository
    ) {
        this.orderPromotionRepository = orderPromotionRepository;
        this.promotionRepository = promotionRepository;
    }

    @Override
    protected void validate(OrderValidationContext context) {
        Long userId = context.getOrder().getUserId();

        for (OrderPromotion orderPromotion : context.getPromotions()) {
            boolean usedBefore = orderPromotionRepository.existsByPromotionIdAndOrder_UserIdAndOrder_StatusNot(
                    orderPromotion.getPromotionId(), userId, OrderStatus.CANCELLED);

            if (usedBefore) {
                String code = promotionRepository.findById(orderPromotion.getPromotionId())
                        .map(promotion -> promotion.getCode())
                        .orElse("#" + orderPromotion.getPromotionId());
                throw new ValidationException(
                        "คุณใช้โค้ด " + code + " ไปแล้ว (ใช้ได้คนละ 1 ครั้งต่อโค้ด)"
                );
            }
        }
    }
}
