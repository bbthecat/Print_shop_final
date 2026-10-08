
package com.printflow.service.strategy.pricing;

import com.printflow.domain.enums.PricingType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PricingCalculator {

    private final PricingStrategyResolver strategyResolver;

    // Constructor Injection
    public PricingCalculator(PricingStrategyResolver strategyResolver) {
        this.strategyResolver = strategyResolver;
    }

    /**
     * คำนวณราคาพิมพ์หลักตามประเภท PricingStrategy
     */
    public BigDecimal calculatePrintPrice(
            PricingType pricingType,
            BigDecimal basePrice,
            int pageCount,
            int copyCount
    ) {
        PricingStrategy strategy = strategyResolver.resolve(pricingType);

        return strategy.calculate(
                basePrice,
                pageCount,
                copyCount
        );
    }

    /**
     * คำนวณราคารวมของ PrintItem
     * = ราคาพิมพ์หลัก + ผลรวมราคาบริการเสริม
     *
     * Add-on แต่ละรายการคิดราคาเพียงครั้งเดียว
     */
    public BigDecimal calculateItemTotal(
            PricingType pricingType,
            BigDecimal basePrice,
            int pageCount,
            int copyCount,
            List<BigDecimal> addonPrices
    ) {
        // คำนวณราคาพิมพ์หลัก
        BigDecimal printPrice = calculatePrintPrice(
                pricingType,
                basePrice,
                pageCount,
                copyCount
        );

        // รวมราคาบริการเสริม โดยคูณจำนวนชุด (copyCount)
        BigDecimal addonsTotal = BigDecimal.ZERO;

        if (addonPrices != null && !addonPrices.isEmpty()) {
            for (BigDecimal addonPrice : addonPrices) {
                if (addonPrice != null
                        && addonPrice.compareTo(BigDecimal.ZERO) > 0) {
                    addonsTotal = addonsTotal.add(addonPrice);
                }
            }
            // addon คิดตามจำนวนชุด
            addonsTotal = addonsTotal.multiply(BigDecimal.valueOf(copyCount));
        }

        // รวมราคาพิมพ์และบริการเสริม
        BigDecimal total = printPrice.add(addonsTotal);

        // ป้องกันยอดรวมติดลบ
        return total.compareTo(BigDecimal.ZERO) < 0
                ? BigDecimal.ZERO
                : total;
    }
}