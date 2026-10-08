package com.printflow.service.strategy.pricing;

import com.printflow.domain.enums.PricingType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BlackWhitePricingStrategy implements PricingStrategy {

    @Override
    public PricingType getPricingType() {
        return PricingType.BLACK_WHITE;
    }

    @Override
    public BigDecimal calculate(BigDecimal basePrice, int pageCount, int copyCount) {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0 || pageCount <= 0 || copyCount <= 0) {
            return BigDecimal.ZERO;
        }
        return basePrice.multiply(BigDecimal.valueOf(pageCount))
                .multiply(BigDecimal.valueOf(copyCount));
    }
}
