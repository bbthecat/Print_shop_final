package com.printflow.controller.web.admin;

import com.printflow.domain.entity.AddonService;
import com.printflow.domain.entity.PrintService;
import com.printflow.dto.form.AdminAddonForm;
import com.printflow.dto.form.AdminServiceForm;
import com.printflow.service.ServiceCatalogCommandService;
import com.printflow.service.ServiceCatalogQueryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/services")
public class AdminServicePageController {

    private static final String VIEW = "admin/services";
    private static final String REDIRECT = "redirect:/admin/services";

    private final ServiceCatalogQueryService queryService;
    private final ServiceCatalogCommandService commandService;

    // Constructor Injection ตามเกณฑ์ห้าม @Autowired บน field
    // Controller เรียก Service เท่านั้น
    public AdminServicePageController(ServiceCatalogQueryService queryService,
                                      ServiceCatalogCommandService commandService) {
        this.queryService = queryService;
        this.commandService = commandService;
    }

    @GetMapping
    public String list(Model model) {
        populateModel(model);
        if (!model.containsAttribute("serviceForm")) {
            model.addAttribute("serviceForm", new AdminServiceForm());
        }
        if (!model.containsAttribute("addonForm")) {
            model.addAttribute("addonForm", new AdminAddonForm());
        }
        return VIEW;
    }

    @PostMapping
    public String createService(@Valid @ModelAttribute("serviceForm") AdminServiceForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            populateModel(model);
            if (!model.containsAttribute("addonForm")) {
                model.addAttribute("addonForm", new AdminAddonForm());
            }
            model.addAttribute("openServiceForm", true);
            return VIEW;
        }

        commandService.createPrintService(form.getName(), form.getDescription(), form.getBasePrice(), form.getPricingType());
        redirect.addFlashAttribute("success", "เพิ่มบริการงานพิมพ์เรียบร้อยแล้ว");
        return REDIRECT;
    }

    @PostMapping("/{id}/deactivate")
    public String deactivateService(@PathVariable Long id, RedirectAttributes redirect) {
        commandService.deactivatePrintService(id);
        redirect.addFlashAttribute("success", "ปิดการใช้งานบริการพิมพ์เรียบร้อยแล้ว");
        return REDIRECT;
    }

    @PostMapping("/addons")
    public String createAddon(@Valid @ModelAttribute("addonForm") AdminAddonForm form,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            populateModel(model);
            if (!model.containsAttribute("serviceForm")) {
                model.addAttribute("serviceForm", new AdminServiceForm());
            }
            model.addAttribute("openAddonForm", true);
            return VIEW;
        }

        commandService.createAddonService(form.getName(), form.getDescription(), form.getPrice());
        redirect.addFlashAttribute("success", "เพิ่มบริการเสริมเรียบร้อยแล้ว");
        return REDIRECT;
    }

    @PostMapping("/addons/{id}/deactivate")
    public String deactivateAddon(@PathVariable Long id, RedirectAttributes redirect) {
        commandService.deactivateAddonService(id);
        redirect.addFlashAttribute("success", "ปิดการใช้งานบริการเสริมเรียบร้อยแล้ว");
        return REDIRECT;
    }

    private void populateModel(Model model) {
        List<PrintService> services = queryService.findAllActivePrintServices();
        List<AddonService> addons = queryService.findAllActiveAddonServices();
        model.addAttribute("services", services);
        model.addAttribute("addons", addons);
    }
}
