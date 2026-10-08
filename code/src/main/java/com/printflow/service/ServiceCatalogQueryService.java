package com.printflow.service;

import com.printflow.domain.entity.PrintService;

public interface ServiceCatalogQueryService {

    PrintService findActiveById(Long id);
}