package com.printflow.validation;

import com.printflow.domain.entity.PrintItem;
import com.printflow.service.ServiceCatalogQueryService;

public class ServiceAvailabilityHandler extends OrderValidationHandler {

    private final ServiceCatalogQueryService serviceCatalogQueryService;

    public ServiceAvailabilityHandler(
            ServiceCatalogQueryService serviceCatalogQueryService
    ) {
        this.serviceCatalogQueryService = serviceCatalogQueryService;
    }

    @Override
    protected void validate(OrderValidationContext context) {

        for (PrintItem item : context.getItems()) {

            serviceCatalogQueryService.findActiveById(
                    item.getServiceId()
            );
        }
    }
}