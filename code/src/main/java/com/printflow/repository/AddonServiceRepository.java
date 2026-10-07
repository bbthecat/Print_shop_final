package com.printflow.repository;

import com.printflow.domain.entity.AddonService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddonServiceRepository extends JpaRepository<AddonService, Long> {
    List<AddonService> findAllByActiveTrue();
    Page<AddonService> findAllByActiveTrue(Pageable pageable);
    Optional<AddonService> findByIdAndActiveTrue(Long id);
}
