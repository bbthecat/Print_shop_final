package com.printflow.controller.web;

import com.printflow.domain.entity.AddonService;
import com.printflow.domain.entity.PrintService;
import com.printflow.domain.entity.Promotion;
import com.printflow.service.PromotionService;
import com.printflow.service.ServiceCatalogQueryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/services")
public class ServiceWebController {

    private final ServiceCatalogQueryService catalogQueryService;
    private final PromotionService promotionService;

    // Constructor Injection ตามเกณฑ์ห้าม @Autowired บน field
    // Controller เรียก Service เท่านั้น ห้ามเรียก Repository ตรง
    public ServiceWebController(ServiceCatalogQueryService catalogQueryService,
                                PromotionService promotionService) {
        this.catalogQueryService = catalogQueryService;
        this.promotionService = promotionService;
    }

    @GetMapping
    public String listServices(Model model) {
        List<PrintService> services = catalogQueryService.findAllActivePrintServices();
        List<AddonService> addons = catalogQueryService.findAllActiveAddonServices();
        List<Promotion> promotions = promotionService.findAllActive();

        model.addAttribute("services", services);
        model.addAttribute("addons", addons);
        model.addAttribute("promotions", promotions);

        return "services/index";
    }
}
