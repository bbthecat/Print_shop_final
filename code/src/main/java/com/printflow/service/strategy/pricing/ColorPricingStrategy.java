package com.printflow.service.strategy.pricing;

import com.printflow.domain.enums.PricingType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ColorPricingStrategy implements PricingStrategy {

    @Override
    public PricingType getPricingType() {
        return PricingType.COLOR;
    }

    @Override
    public BigDecimal calculate(BigDecimal basePrice, int pageCount, int copyCount) {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0 || pageCount <= 0 || copyCount <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = basePrice.multiply(BigDecimal.valueOf(pageCount))
                .multiply(BigDecimal.valueOf(copyCount));

        // งานพิมพ์สี: หากจำนวนหน้าเกิน 50 หน้าต่อชุด ได้รับส่วนลดปริมาณ (Volume Discount) 10%
        if (pageCount > 50) {
            total = total.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP);
        }

        return total;
    }
}
