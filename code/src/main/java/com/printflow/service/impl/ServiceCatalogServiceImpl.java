package com.printflow.service.impl;

import com.printflow.domain.entity.AddonService;
import com.printflow.domain.entity.PrintService;
import com.printflow.domain.enums.PricingType;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.repository.AddonServiceRepository;
import com.printflow.repository.PrintServiceRepository;
import com.printflow.service.ServiceCatalogCommandService;
import com.printflow.service.ServiceCatalogQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ServiceCatalogServiceImpl implements ServiceCatalogQueryService, ServiceCatalogCommandService {

    private final PrintServiceRepository printServiceRepository;
    private final AddonServiceRepository addonServiceRepository;

    // Constructor Injection ตามเกณฑ์ห้าม @Autowired บน field
    public ServiceCatalogServiceImpl(PrintServiceRepository printServiceRepository,
                                     AddonServiceRepository addonServiceRepository) {
        this.printServiceRepository = printServiceRepository;
        this.addonServiceRepository = addonServiceRepository;
    }

    // --- Query Service ---

    @Override
    @Transactional(readOnly = true)
    public List<PrintService> findAllPrintServices() {
        return printServiceRepository.findAll(Sort.by("id"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddonService> findAllAddonServices() {
        return addonServiceRepository.findAll(Sort.by("id"));
    }

    @Override
    @Transactional(readOnly = true)
    public PrintService findPrintServiceById(Long id) {
        return printServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Print service not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public AddonService findAddonServiceById(Long id) {
        return addonServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Addon service not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PrintService findActivePrintServiceById(Long id) {
        return printServiceRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Print service not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrintService> findAllActivePrintServices() {
        return printServiceRepository.findAllByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrintService> findAllActivePrintServices(Pageable pageable) {
        return printServiceRepository.findAllByActiveTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public AddonService findActiveAddonServiceById(Long id) {
        return addonServiceRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Addon service not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddonService> findAllActiveAddonServices() {
        return addonServiceRepository.findAllByActiveTrue();
    }

    // --- Command Service ---

    @Override
    @Transactional
    public PrintService createPrintService(String name, String description, BigDecimal basePrice, PricingType pricingType) {
        PrintService service = new PrintService();
        service.setName(name);
        service.setDescription(description);
        service.setBasePrice(basePrice);
        service.setPricingType(pricingType);
        service.setActive(true);
        return printServiceRepository.save(service);
    }

    @Override
    @Transactional
    public PrintService updatePrintService(Long id, String name, String description, BigDecimal basePrice, PricingType pricingType, Boolean active) {
        PrintService service = printServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Print service not found with ID: " + id));

        if (name != null) service.setName(name);
        if (description != null) service.setDescription(description);
        if (basePrice != null) service.setBasePrice(basePrice);
        if (pricingType != null) service.setPricingType(pricingType);
        if (active != null) service.setActive(active);

        return printServiceRepository.save(service);
    }

    @Override
    @Transactional
    public void deactivatePrintService(Long id) {
        PrintService service = printServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Print service not found with ID: " + id));
        service.setActive(false);
        printServiceRepository.save(service);
    }

    @Override
    @Transactional
    public AddonService createAddonService(String name, String description, BigDecimal price) {
        AddonService addon = new AddonService();
        addon.setName(name);
        addon.setDescription(description);
        addon.setPrice(price);
        addon.setActive(true);
        return addonServiceRepository.save(addon);
    }

    @Override
    @Transactional
    public AddonService updateAddonService(Long id, String name, String description, BigDecimal price, Boolean active) {
        AddonService addon = addonServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Addon service not found with ID: " + id));

        if (name != null) addon.setName(name);
        if (description != null) addon.setDescription(description);
        if (price != null) addon.setPrice(price);
        if (active != null) addon.setActive(active);

        return addonServiceRepository.save(addon);
    }

    @Override
    @Transactional
    public void deactivateAddonService(Long id) {
        AddonService addon = addonServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Addon service not found with ID: " + id));
        addon.setActive(false);
        addonServiceRepository.save(addon);
    }
}
