package com.printflow.validation;

import com.printflow.domain.entity.OrderPromotion;
import com.printflow.domain.entity.Promotion;
import com.printflow.exception.ValidationException;
import com.printflow.repository.PromotionRepository;

import java.time.LocalDateTime;

public class PromotionValidityHandler extends OrderValidationHandler {

    private final PromotionRepository promotionRepository;

    public PromotionValidityHandler(
            PromotionRepository promotionRepository
    ) {
        this.promotionRepository = promotionRepository;
    }

    @Override
    protected void validate(OrderValidationContext context) {

        for (OrderPromotion orderPromotion : context.getPromotions()) {

            Promotion promotion = promotionRepository
                    .findById(orderPromotion.getPromotionId())
                    .orElseThrow(() ->
                            new ValidationException(
                                    "Promotion not found: "
                                            + orderPromotion.getPromotionId()
                            )
                    );

            LocalDateTime now = LocalDateTime.now();

            if (!promotion.isActive()) {
                throw new ValidationException(
                        "Promotion is inactive: " + promotion.getCode()
                );
            }

            if (now.isBefore(promotion.getStartDate())
                    || now.isAfter(promotion.getEndDate())) {

                throw new ValidationException(
                        "Promotion is not valid at this time: "
                                + promotion.getCode()
                );
            }
        }
    }
}