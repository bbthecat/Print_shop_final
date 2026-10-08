package com.printflow.controller.web.admin;

import com.printflow.exception.ValidationException;
import com.printflow.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportPageController {

    private final ReportService reportService;

    public AdminReportPageController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String report(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                         Model model) {
        try {
            model.addAttribute("report", reportService.getSummary(from, to));
        } catch (ValidationException ex) {
            // ช่วงวันที่ไม่ถูกต้อง: แจ้งเตือนแล้วแสดงรายงาน 30 วันล่าสุดแทน
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("report", reportService.getSummary(null, null));
        }
        return "admin/reports";
    }
}
