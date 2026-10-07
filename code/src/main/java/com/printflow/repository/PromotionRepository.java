package com.printflow.repository;

import com.printflow.domain.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByCode(String code);
    Optional<Promotion> findByCodeAndActiveTrue(String code);
    List<Promotion> findAllByActiveTrue();
    Page<Promotion> findAllByActiveTrue(Pageable pageable);
    boolean existsByCode(String code);
}
