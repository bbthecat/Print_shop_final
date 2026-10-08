package com.printflow.service;

import com.printflow.domain.entity.AddonService;
import com.printflow.domain.entity.PrintService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ServiceCatalogQueryService {

    PrintService findActivePrintServiceById(Long id);

    List<PrintService> findAllActivePrintServices();

    Page<PrintService> findAllActivePrintServices(Pageable pageable);

    AddonService findActiveAddonServiceById(Long id);

    List<AddonService> findAllActiveAddonServices();
}
