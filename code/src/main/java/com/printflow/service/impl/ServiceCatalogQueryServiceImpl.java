package com.printflow.service.impl;

import com.printflow.domain.entity.PrintService;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.repository.PrintServiceRepository;
import com.printflow.service.ServiceCatalogQueryService;
import org.springframework.stereotype.Service;

@Service
public class ServiceCatalogQueryServiceImpl implements ServiceCatalogQueryService {

    private final PrintServiceRepository printServiceRepository;

    public ServiceCatalogQueryServiceImpl(
            PrintServiceRepository printServiceRepository
    ) {
        this.printServiceRepository = printServiceRepository;
    }

    @Override
    public PrintService findActiveById(Long id) {
        return printServiceRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Print service not found or inactive: " + id
                        )
                );
    }
}