package com.printflow.controller.web;

import com.printflow.service.ServiceCatalogQueryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ServiceCatalogQueryService catalogQueryService;

    public HomeController(ServiceCatalogQueryService catalogQueryService) {
        this.catalogQueryService = catalogQueryService;
    }

    // หน้าแรกแสดงราคาพิมพ์จริงจากบริการที่เปิดอยู่
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("services", catalogQueryService.findAllActivePrintServices());
        model.addAttribute("addons", catalogQueryService.findAllActiveAddonServices());
        return "index";
    }
}
