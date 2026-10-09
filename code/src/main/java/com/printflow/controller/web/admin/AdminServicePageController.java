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
    private static final String SERVICE_EDIT_VIEW = "admin/service-edit";
    private static final String ADDON_EDIT_VIEW = "admin/addon-edit";
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

    @PostMapping("/{id}/activate")
    public String activateService(@PathVariable Long id, RedirectAttributes redirect) {
        commandService.updatePrintService(id, null, null, null, null, true);
        redirect.addFlashAttribute("success", "เปิดใช้งานบริการพิมพ์อีกครั้งเรียบร้อยแล้ว");
        return REDIRECT;
    }

    @GetMapping("/{id}/edit")
    public String editServiceForm(@PathVariable Long id, Model model) {
        PrintService service = queryService.findPrintServiceById(id);
        AdminServiceForm form = new AdminServiceForm();
        form.setName(service.getName());
        form.setDescription(service.getDescription());
        form.setBasePrice(service.getBasePrice());
        form.setPricingType(service.getPricingType());
        model.addAttribute("serviceId", id);
        model.addAttribute("serviceForm", form);
        return SERVICE_EDIT_VIEW;
    }

    @PostMapping("/{id}/edit")
    public String updateService(@PathVariable Long id,
                                @Valid @ModelAttribute("serviceForm") AdminServiceForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("serviceId", id);
            return SERVICE_EDIT_VIEW;
        }
        commandService.updatePrintService(id, form.getName(), form.getDescription(), form.getBasePrice(),
                form.getPricingType(), null);
        redirect.addFlashAttribute("success", "แก้ไขบริการ " + form.getName() + " เรียบร้อยแล้ว");
        return REDIRECT;
    }

    @PostMapping("/addons/{id}/activate")
    public String activateAddon(@PathVariable Long id, RedirectAttributes redirect) {
        commandService.updateAddonService(id, null, null, null, true);
        redirect.addFlashAttribute("success", "เปิดใช้งานบริการเสริมอีกครั้งเรียบร้อยแล้ว");
        return REDIRECT;
    }

    @GetMapping("/addons/{id}/edit")
    public String editAddonForm(@PathVariable Long id, Model model) {
        AddonService addon = queryService.findAddonServiceById(id);
        AdminAddonForm form = new AdminAddonForm();
        form.setName(addon.getName());
        form.setDescription(addon.getDescription());
        form.setPrice(addon.getPrice());
        model.addAttribute("addonId", id);
        model.addAttribute("addonForm", form);
        return ADDON_EDIT_VIEW;
    }

    @PostMapping("/addons/{id}/edit")
    public String updateAddon(@PathVariable Long id,
                              @Valid @ModelAttribute("addonForm") AdminAddonForm form,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("addonId", id);
            return ADDON_EDIT_VIEW;
        }
        commandService.updateAddonService(id, form.getName(), form.getDescription(), form.getPrice(), null);
        redirect.addFlashAttribute("success", "แก้ไขบริการเสริม " + form.getName() + " เรียบร้อยแล้ว");
        return REDIRECT;
    }

    private void populateModel(Model model) {
        // แสดงทั้งที่เปิดและปิดใช้งาน เพื่อให้ Admin เปิดใช้งานใหม่หรือแก้ไขได้
        List<PrintService> services = queryService.findAllPrintServices();
        List<AddonService> addons = queryService.findAllAddonServices();
        model.addAttribute("services", services);
        model.addAttribute("addons", addons);
    }
}
