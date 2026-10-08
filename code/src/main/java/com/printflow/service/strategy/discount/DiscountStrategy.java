package com.printflow.service.strategy.discount;

import com.printflow.domain.entity.Promotion;
import com.printflow.domain.enums.DiscountType;

import java.math.BigDecimal;

public interface DiscountStrategy {

    DiscountType getDiscountType();

    BigDecimal calculate(BigDecimal subtotal, Promotion promotion);
}
