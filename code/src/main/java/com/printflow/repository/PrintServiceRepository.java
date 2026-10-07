package com.printflow.repository;

import com.printflow.domain.entity.PrintService;
import com.printflow.domain.enums.PricingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrintServiceRepository extends JpaRepository<PrintService, Long> {
    List<PrintService> findAllByActiveTrue();
    Page<PrintService> findAllByActiveTrue(Pageable pageable);
    Optional<PrintService> findByIdAndActiveTrue(Long id);
    List<PrintService> findAllByPricingTypeAndActiveTrue(PricingType pricingType);
}
