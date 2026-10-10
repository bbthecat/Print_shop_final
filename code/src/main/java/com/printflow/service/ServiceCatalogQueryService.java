package com.printflow.service;

import com.printflow.domain.entity.AddonService;
import com.printflow.domain.entity.PrintService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ServiceCatalogQueryService {

    PrintService findActivePrintServiceById(Long id);

    List<PrintService> findAllActivePrintServices();

    Page<PrintService> findAllActivePrintServices(Pageable pageable);

    AddonService findActiveAddonServiceById(Long id);

    List<AddonService> findAllActiveAddonServices();

    // สำหรับหน้า Admin: รวมรายการที่ปิดใช้งานแล้วด้วย (เพื่อแก้ไข / เปิดใช้งานใหม่ได้)
    List<PrintService> findAllPrintServices();

    List<AddonService> findAllAddonServices();

    PrintService findPrintServiceById(Long id);

    AddonService findAddonServiceById(Long id);
}