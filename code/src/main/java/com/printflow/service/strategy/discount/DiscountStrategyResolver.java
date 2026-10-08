package com.printflow.service.strategy.discount;

import com.printflow.domain.enums.DiscountType;
import com.printflow.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class DiscountStrategyResolver {

    private final Map<DiscountType, DiscountStrategy> strategies;

    // Constructor Injection ตามเกณฑ์ห้ามใช้ @Autowired บน field
    public DiscountStrategyResolver(List<DiscountStrategy> strategyList) {
        Map<DiscountType, DiscountStrategy> map = new EnumMap<>(DiscountType.class);
        for (DiscountStrategy strategy : strategyList) {
            map.put(strategy.getDiscountType(), strategy);
        }
        this.strategies = Collections.unmodifiableMap(map);
    }

    public DiscountStrategy resolve(DiscountType discountType) {
        if (discountType == null) {
            throw new ValidationException("Discount type must not be null");
        }
        DiscountStrategy strategy = strategies.get(discountType);
        if (strategy == null) {
            throw new ValidationException("No discount strategy found for type: " + discountType);
        }
        return strategy;
    }
}
