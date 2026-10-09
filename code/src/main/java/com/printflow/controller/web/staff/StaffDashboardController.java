package com.printflow.controller.web.staff;

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

    public StaffDashboardController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/staff/dashboard")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("today", reportService.getSummary(today, today));
        model.addAttribute("last30Days", reportService.getSummary(null, null));
        return "staff/dashboard";
    }
}
