package com.printflow.config;

import com.printflow.repository.OrderPromotionRepository;
import com.printflow.repository.PromotionRepository;
import com.printflow.service.ServiceCatalogQueryService;
import com.printflow.validation.FileTypeValidationHandler;
import com.printflow.validation.OrderValidationHandler;
import com.printflow.validation.PromotionUsageLimitHandler;
import com.printflow.validation.PromotionValidityHandler;
import com.printflow.validation.QuantityValidationHandler;
import com.printflow.validation.ServiceAvailabilityHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderValidationChainConfig {

    @Bean
    public OrderValidationHandler orderValidationChain(
            ServiceCatalogQueryService serviceCatalogQueryService,
            PromotionRepository promotionRepository,
            OrderPromotionRepository orderPromotionRepository
    ) {

        OrderValidationHandler serviceAvailability =
                new ServiceAvailabilityHandler(serviceCatalogQueryService);

        OrderValidationHandler fileType =
                new FileTypeValidationHandler();

        OrderValidationHandler quantity =
                new QuantityValidationHandler();

        OrderValidationHandler promotion =
                new PromotionValidityHandler(promotionRepository);

        OrderValidationHandler promotionUsageLimit =
                new PromotionUsageLimitHandler(orderPromotionRepository, promotionRepository);

        serviceAvailability
                .setNext(fileType)
                .setNext(quantity)
                .setNext(promotion)
                .setNext(promotionUsageLimit);

        return serviceAvailability;
    }
}