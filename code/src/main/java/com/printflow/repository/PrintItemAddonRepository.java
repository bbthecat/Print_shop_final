package com.printflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.printflow.domain.entity.PrintItemAddon;

public interface PrintItemAddonRepository
        extends JpaRepository<PrintItemAddon, Long> {

    List<PrintItemAddon> findByItemId(Long itemId);
}