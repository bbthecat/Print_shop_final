package com.printflow.service.impl;

import com.printflow.domain.entity.Promotion;
import com.printflow.domain.enums.DiscountType;
import com.printflow.exception.DuplicateResourceException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.exception.ValidationException;
import com.printflow.repository.OrderPromotionRepository;
import com.printflow.repository.PromotionRepository;
import com.printflow.service.PromotionService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final OrderPromotionRepository orderPromotionRepository;

    // Constructor Injection ตามเกณฑ์ห้าม @Autowired บน field
    public PromotionServiceImpl(PromotionRepository promotionRepository,
                                OrderPromotionRepository orderPromotionRepository) {
        this.promotionRepository = promotionRepository;
        this.orderPromotionRepository = orderPromotionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Promotion findValidByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Promotion code must not be blank");
        }

        Promotion promo = promotionRepository.findByCodeAndActiveTrue(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found or inactive: " + code));

        LocalDateTime now = LocalDateTime.now();
        if (promo.getStartDate() != null && now.isBefore(promo.getStartDate())) {
            throw new ValidationException("Promotion is not active yet: " + code);
        }
        if (promo.getEndDate() != null && now.isAfter(promo.getEndDate())) {
            throw new ValidationException("Promotion has expired: " + code);
        }

        return promo;
    }

    @Override
    @Transactional(readOnly = true)
    public Promotion findById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Promotion> findAllActive() {
        // แสดงลูกค้าเฉพาะโปรที่เปิดอยู่และอยู่ในช่วงวันที่ใช้ได้ตอนนี้
        LocalDateTime now = LocalDateTime.now();
        return promotionRepository.findAllByActiveTrue().stream()
                .filter(p -> p.getStartDate() == null || !p.getStartDate().isAfter(now))
                .filter(p -> p.getEndDate() == null || !p.getEndDate().isBefore(now))
                .toList();
    }

    @Override
    @Transactional
    public Promotion create(String code, String description, DiscountType discountType,
                            BigDecimal discountValue, BigDecimal minOrderAmount,
                            LocalDateTime startDate, LocalDateTime endDate) {
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Promotion code is required");
        }
        validateRules(discountType, discountValue, startDate, endDate);

        String normalizedCode = code.trim().toUpperCase();
        if (promotionRepository.existsByCode(normalizedCode)) {
            throw new DuplicateResourceException("Promotion code already exists: " + normalizedCode);
        }

        Promotion promo = new Promotion();
        promo.setCode(normalizedCode);
        promo.setDescription(description);
        promo.setDiscountType(discountType);
        promo.setDiscountValue(discountValue);
        promo.setMinOrderAmount(minOrderAmount != null ? minOrderAmount : BigDecimal.ZERO);
        promo.setStartDate(startDate);
        promo.setEndDate(endDate);
        promo.setActive(true);

        return promotionRepository.save(promo);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Promotion promo = findById(id);
        long used = orderPromotionRepository.countByPromotionId(id);
        if (used > 0) {
            throw new ValidationException("โค้ด " + promo.getCode() + " ถูกใช้ไปแล้ว " + used
                    + " ครั้ง ลบไม่ได้ ให้ปิดใช้งานแทน");
        }
        promotionRepository.delete(promo);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Promotion promo = findById(id);
        promo.setActive(false);
        promotionRepository.save(promo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Promotion> findAll() {
        return promotionRepository.findAll(Sort.by("id"));
    }

    @Override
    @Transactional
    public Promotion update(Long id, String description, DiscountType discountType,
                            BigDecimal discountValue, BigDecimal minOrderAmount,
                            LocalDateTime startDate, LocalDateTime endDate) {
        validateRules(discountType, discountValue, startDate, endDate);

        Promotion promo = findById(id);
        promo.setDescription(description);
        promo.setDiscountType(discountType);
        promo.setDiscountValue(discountValue);
        promo.setMinOrderAmount(minOrderAmount != null ? minOrderAmount : BigDecimal.ZERO);
        promo.setStartDate(startDate);
        promo.setEndDate(endDate);
        return promotionRepository.save(promo);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        Promotion promo = findById(id);
        promo.setActive(true);
        promotionRepository.save(promo);
    }

    // กฎที่ต้องตรวจทั้งตอนสร้างและแก้ไข (ทั้งจาก API และหน้าเว็บ)
    private void validateRules(DiscountType discountType, BigDecimal discountValue,
                               LocalDateTime startDate, LocalDateTime endDate) {
        if (discountType == DiscountType.PERCENTAGE && discountValue != null
                && discountValue.compareTo(new BigDecimal("100")) > 0) {
            throw new ValidationException("Percentage discount value cannot exceed 100%");
        }
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new ValidationException("End date must be after start date");
        }
    }
}
