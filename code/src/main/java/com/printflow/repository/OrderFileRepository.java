package com.printflow.repository;

import com.printflow.domain.entity.OrderFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderFileRepository extends JpaRepository<OrderFile, Long> {

    List<OrderFile> findByOrderId(Long orderId);
}