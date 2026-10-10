package com.printflow.repository;

import com.printflow.domain.entity.PrintItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrintItemRepository extends JpaRepository<PrintItem, Long> {

    List<PrintItem> findByOrderId(Long orderId);
}