package com.printflow.controller.web.admin;

import com.printflow.domain.entity.Promotion;
import com.printflow.domain.enums.DiscountType;
import com.printflow.dto.form.AdminPromotionForm;
import com.printflow.exception.DuplicateResourceException;
import com.printflow.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/promotions")
public class AdminPromotionPageController {

    private static final String VIEW = "admin/promotions";
    private static final String EDIT_VIEW = "admin/promotion-edit";
    private static final String REDIRECT = "redirect:/admin/promotions";

    private final PromotionService promotionService;

    // Constructor Injection ตามเกณฑ์ห้าม @Autowired บน field
    // Controller เรียก Service เท่านั้น
    public AdminPromotionPageController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public String list(Model model) {
        populateModel(model);
        if (!model.containsAttribute("form")) {
            AdminPromotionForm defaultForm = new AdminPromotionForm();
            defaultForm.setStartDate(LocalDateTime.now().withSecond(0).withNano(0));
            defaultForm.setEndDate(LocalDateTime.now().plusMonths(1).withSecond(0).withNano(0));
            model.addAttribute("form", defaultForm);
        }
        return VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") AdminPromotionForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (form.getStartDate() != null && form.getEndDate() != null && form.getEndDate().isBefore(form.getStartDate())) {
            bindingResult.rejectValue("endDate", "invalid", "วันสิ้นสุดต้องอยู่หลังวันเริ่มต้น");
        }
        if (form.getDiscountType() == DiscountType.PERCENTAGE && form.getDiscountValue() != null && form.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
            bindingResult.rejectValue("discountValue", "invalid", "ส่วนลดแบบเปอร์เซ็นต์ต้องไม่เกิน 100%");
        }

        if (bindingResult.hasErrors()) {
            populateModel(model);
            model.addAttribute("openForm", true);
            return VIEW;
        }

        try {
            promotionService.create(
                    form.getCode(),
                    form.getDescription(),
                    form.getDiscountType(),
                    form.getDiscountValue(),
                    form.getMinOrderAmount(),
                    form.getStartDate(),
                    form.getEndDate()
            );
            redirect.addFlashAttribute("success", "เพิ่มโปรโมชัน " + form.getCode().trim().toUpperCase() + " เรียบร้อยแล้ว");
            return REDIRECT;
        } catch (DuplicateResourceException e) {
            bindingResult.rejectValue("code", "duplicate", e.getMessage());
            populateModel(model);
            model.addAttribute("openForm", true);
            return VIEW;
        }
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirect) {
        promotionService.deactivate(id);
        redirect.addFlashAttribute("success", "ปิดการใช้งานโปรโมชันเรียบร้อยแล้ว");
        return REDIRECT;
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes redirect) {
        promotionService.activate(id);
        redirect.addFlashAttribute("success", "เปิดใช้งานโปรโมชันอีกครั้งเรียบร้อยแล้ว");
        return REDIRECT;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Promotion promo = promotionService.findById(id);
        AdminPromotionForm form = new AdminPromotionForm();
        form.setCode(promo.getCode());
        form.setDescription(promo.getDescription());
        form.setDiscountType(promo.getDiscountType());
        form.setDiscountValue(promo.getDiscountValue());
        form.setMinOrderAmount(promo.getMinOrderAmount());
        form.setStartDate(promo.getStartDate());
        form.setEndDate(promo.getEndDate());
        model.addAttribute("promotionId", id);
        model.addAttribute("form", form);
        return EDIT_VIEW;
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") AdminPromotionForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (form.getStartDate() != null && form.getEndDate() != null && !form.getEndDate().isAfter(form.getStartDate())) {
            bindingResult.rejectValue("endDate", "invalid", "วันสิ้นสุดต้องอยู่หลังวันเริ่มต้น");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("promotionId", id);
            return EDIT_VIEW;
        }
        promotionService.update(id, form.getDescription(), form.getDiscountType(), form.getDiscountValue(),
                form.getMinOrderAmount(), form.getStartDate(), form.getEndDate());
        redirect.addFlashAttribute("success", "แก้ไขโปรโมชัน " + form.getCode() + " เรียบร้อยแล้ว");
        return REDIRECT;
    }

    private void populateModel(Model model) {
        // แสดงทั้งที่เปิดและปิดใช้งาน เพื่อให้ Admin เปิดใช้งานใหม่หรือแก้ไขได้
        List<Promotion> promotions = promotionService.findAll();
        model.addAttribute("promotions", promotions);
    }
}
