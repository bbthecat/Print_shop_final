package com.printflow.controller.web;

import com.printflow.dto.form.ProfileForm;
import com.printflow.dto.response.CustomerResponse;
import com.printflow.exception.DuplicateResourceException;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * UC-03: ลูกค้าดูและแก้ไขข้อมูลส่วนตัวของตัวเอง
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final String VIEW = "profile/index";

    private final CustomerService customerService;
    private final CurrentUserProvider currentUserProvider;

    public ProfileController(CustomerService customerService, CurrentUserProvider currentUserProvider) {
        this.customerService = customerService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public String show(Model model) {
        CustomerResponse customer = customerService.getById(currentUserProvider.getCurrentUserId());
        model.addAttribute("customer", customer);
        model.addAttribute("form", ProfileForm.from(customer));
        return VIEW;
    }

    @PostMapping
    public String update(@Valid @ModelAttribute("form") ProfileForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        Long userId = currentUserProvider.getCurrentUserId();
        if (bindingResult.hasErrors()) {
            model.addAttribute("customer", customerService.getById(userId));
            return VIEW;
        }
        try {
            customerService.update(userId, form.toRequest());
        } catch (DuplicateResourceException ex) {
            model.addAttribute("customer", customerService.getById(userId));
            model.addAttribute("error", ex.getMessage());
            return VIEW;
        }
        redirect.addFlashAttribute("success", "บันทึกข้อมูลส่วนตัวเรียบร้อยแล้ว");
        return "redirect:/profile";
    }
}
