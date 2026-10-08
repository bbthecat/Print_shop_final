package com.printflow.mapper;

import com.printflow.domain.entity.Promotion;
import com.printflow.dto.request.PromotionRequest;
import com.printflow.dto.response.PromotionResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PromotionMapper {

    public Promotion toEntity(PromotionRequest request) {
        Promotion promo = new Promotion();
        promo.setCode(request.code() != null ? request.code().trim().toUpperCase() : null);
        promo.setDescription(request.description());
        promo.setDiscountType(request.discountType());
        promo.setDiscountValue(request.discountValue());
        promo.setMinOrderAmount(request.minOrderAmount() != null ? request.minOrderAmount() : BigDecimal.ZERO);
        promo.setStartDate(request.startDate());
        promo.setEndDate(request.endDate());
        promo.setActive(true);
        return promo;
    }

    public PromotionResponse toResponse(Promotion promo) {
        if (promo == null) return null;
        return new PromotionResponse(
                promo.getId(),
                promo.getCode(),
                promo.getDescription(),
                promo.getDiscountType(),
                promo.getDiscountValue(),
                promo.getMinOrderAmount(),
                promo.getStartDate(),
                promo.getEndDate(),
                promo.isActive(),
                promo.getCreatedAt(),
                promo.getUpdatedAt()
        );
    }
}
