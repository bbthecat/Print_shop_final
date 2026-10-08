package com.printflow.service.strategy.pricing;

import com.printflow.domain.enums.PricingType;
import com.printflow.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PricingStrategyResolver {

    private final Map<PricingType, PricingStrategy> strategies;

    // Constructor Injection ตามเกณฑ์ห้ามใช้ @Autowired บน field
    public PricingStrategyResolver(List<PricingStrategy> strategyList) {
        Map<PricingType, PricingStrategy> map = new EnumMap<>(PricingType.class);
        for (PricingStrategy strategy : strategyList) {
            map.put(strategy.getPricingType(), strategy);
        }
        this.strategies = Collections.unmodifiableMap(map);
    }

    public PricingStrategy resolve(PricingType pricingType) {
        if (pricingType == null) {
            throw new ValidationException("Pricing type must not be null");
        }
        PricingStrategy strategy = strategies.get(pricingType);
        if (strategy == null) {
            throw new ValidationException("No pricing strategy found for type: " + pricingType);
        }
        return strategy;
    }
}
