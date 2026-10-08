package com.printflow.service;

import com.printflow.domain.entity.AddonService;
import com.printflow.domain.entity.PrintService;
import com.printflow.domain.enums.PricingType;

import java.math.BigDecimal;

public interface ServiceCatalogCommandService {

    PrintService createPrintService(String name, String description, BigDecimal basePrice, PricingType pricingType);

    PrintService updatePrintService(Long id, String name, String description, BigDecimal basePrice, PricingType pricingType, Boolean active);

    void deactivatePrintService(Long id);

    AddonService createAddonService(String name, String description, BigDecimal price);

    AddonService updateAddonService(Long id, String name, String description, BigDecimal price, Boolean active);

    void deactivateAddonService(Long id);
}
