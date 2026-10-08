package com.printflow.service.strategy.pricing;

import com.printflow.domain.enums.PricingType;

import java.math.BigDecimal;

public interface PricingStrategy {

    PricingType getPricingType();

    BigDecimal calculate(BigDecimal basePrice, int pageCount, int copyCount);
}
