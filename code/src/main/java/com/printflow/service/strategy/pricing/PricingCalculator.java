package com.printflow.service.strategy.pricing;

import com.printflow.domain.enums.PricingType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PricingCalculator {

    private final PricingStrategyResolver strategyResolver;

    // Constructor Injection ตามเกณฑ์ห้ามใช้ @Autowired บน field
    public PricingCalculator(PricingStrategyResolver strategyResolver) {
        this.strategyResolver = strategyResolver;
    }

    /**
     * คำนวณราคาพิมพ์หลักตามประเภท PricingStrategy
     */
    public BigDecimal calculatePrintPrice(PricingType pricingType, BigDecimal basePrice, int pageCount, int copyCount) {
        PricingStrategy strategy = strategyResolver.resolve(pricingType);
        return strategy.calculate(basePrice, pageCount, copyCount);
    }

    /**
     * คำนวณราคารวมของ 1 PrintItem: ราคาพิมพ์หลัก + ผลรวมราคาบริการเสริม (Addon) คูณจำนวนชุด
     */
    public BigDecimal calculateItemTotal(PricingType pricingType, BigDecimal basePrice, int pageCount, int copyCount, List<BigDecimal> addonPrices) {
        BigDecimal printPrice = calculatePrintPrice(pricingType, basePrice, pageCount, copyCount);

        BigDecimal addonsTotal = BigDecimal.ZERO;
        if (addonPrices != null && !addonPrices.isEmpty()) {
            for (BigDecimal addonPrice : addonPrices) {
                if (addonPrice != null && addonPrice.compareTo(BigDecimal.ZERO) > 0) {
                    addonsTotal = addonsTotal.add(addonPrice.multiply(BigDecimal.valueOf(Math.max(1, copyCount))));
                }
            }
        }

        BigDecimal total = printPrice.add(addonsTotal);
        return total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
    }
}
