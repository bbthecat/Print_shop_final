package com.printflow.controller.web.staff;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.service.OrderQueryService;
import com.printflow.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

/**
 * แดชบอร์ดของพนักงาน: งานวันนี้ + ภาพรวม 30 วัน (ใช้ ReportService ตัวเดียวกับหน้ารายงาน)
 */
@Controller
public class StaffDashboardController {

    private final ReportService reportService;
    private final OrderQueryService orderQueryService;

    public StaffDashboardController(ReportService reportService, OrderQueryService orderQueryService) {
        this.reportService = reportService;
        this.orderQueryService = orderQueryService;
    }

    @GetMapping("/staff/dashboard")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("today", reportService.getSummary(today, today));
        model.addAttribute("last30Days", reportService.getSummary(null, null));
        // งานที่ค้างรอยืนยันทั้งหมด รวมที่สั่งมาก่อนวันนี้
        model.addAttribute("pendingCount", orderQueryService.countByStatus(OrderStatus.PENDING));
        return "staff/dashboard";
    }
}
