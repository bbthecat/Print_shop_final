package com.printflow.mapper;

import com.printflow.domain.entity.AddonService;
import com.printflow.domain.entity.PrintService;
import com.printflow.dto.request.AddonServiceRequest;
import com.printflow.dto.request.PrintServiceRequest;
import com.printflow.dto.response.AddonServiceResponse;
import com.printflow.dto.response.PrintServiceResponse;
import org.springframework.stereotype.Component;

@Component
public class ServiceCatalogMapper {

    public PrintService toEntity(PrintServiceRequest request) {
        PrintService service = new PrintService();
        service.setName(request.name());
        service.setDescription(request.description());
        service.setBasePrice(request.basePrice());
        service.setPricingType(request.pricingType());
        service.setActive(true);
        return service;
    }

    public PrintServiceResponse toResponse(PrintService service) {
        if (service == null) return null;
        return new PrintServiceResponse(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getBasePrice(),
                service.getPricingType(),
                service.isActive(),
                service.getCreatedAt(),
                service.getUpdatedAt()
        );
    }

    public AddonService toEntity(AddonServiceRequest request) {
        AddonService addon = new AddonService();
        addon.setName(request.name());
        addon.setDescription(request.description());
        addon.setPrice(request.price());
        addon.setActive(true);
        return addon;
    }

    public AddonServiceResponse toResponse(AddonService addon) {
        if (addon == null) return null;
        return new AddonServiceResponse(
                addon.getId(),
                addon.getName(),
                addon.getDescription(),
                addon.getPrice(),
                addon.isActive(),
                addon.getCreatedAt(),
                addon.getUpdatedAt()
        );
    }
}
