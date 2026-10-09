package com.printflow.service;

import com.printflow.domain.entity.Promotion;
import com.printflow.domain.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PromotionService {

    Promotion findValidByCode(String code);

    Promotion findById(Long id);

    List<Promotion> findAllActive();

    Promotion create(String code, String description, DiscountType discountType,
                     BigDecimal discountValue, BigDecimal minOrderAmount,
                     LocalDateTime startDate, LocalDateTime endDate);

    void deactivate(Long id);

    // สำหรับหน้า Admin
    List<Promotion> findAll();

    Promotion update(Long id, String description, DiscountType discountType,
                     BigDecimal discountValue, BigDecimal minOrderAmount,
                     LocalDateTime startDate, LocalDateTime endDate);

    void activate(Long id);
}
