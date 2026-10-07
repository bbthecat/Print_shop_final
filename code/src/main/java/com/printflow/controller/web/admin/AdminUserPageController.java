package com.printflow.controller.web.admin;

import com.printflow.domain.enums.Role;
import com.printflow.dto.form.AdminUserForm;
import com.printflow.exception.DuplicateResourceException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.exception.ValidationException;
import com.printflow.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserPageController {

    private static final String VIEW = "admin/users";
    private static final String REDIRECT = "redirect:/admin/users";

    private final AdminUserService adminUserService;

    public AdminUserPageController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Role role,
                       @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
                       Model model) {
        populateList(model, role, pageable);
        model.addAttribute("form", new AdminUserForm());
        return VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") AdminUserForm form,
                         BindingResult bindingResult,
                         @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
                         Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            populateList(model, null, pageable);
            model.addAttribute("openCreateForm", true);
            return VIEW;
        }
        try {
            adminUserService.create(form.toRequest());
        } catch (DuplicateResourceException ex) {
            populateList(model, null, pageable);
            model.addAttribute("openCreateForm", true);
            model.addAttribute("error", ex.getMessage());
            return VIEW;
        }
        redirect.addFlashAttribute("success", "สร้างบัญชี " + form.getUsername() + " เรียบร้อย");
        return REDIRECT;
    }

    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam Role role, RedirectAttributes redirect) {
        try {
            adminUserService.changeRole(id, role);
            redirect.addFlashAttribute("success", "เปลี่ยน role เป็น " + role + " แล้ว");
        } catch (ValidationException | ResourceNotFoundException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return REDIRECT;
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam boolean active, RedirectAttributes redirect) {
        try {
            adminUserService.changeActive(id, active);
            redirect.addFlashAttribute("success", active ? "เปิดใช้งานบัญชีแล้ว" : "ปิดใช้งานบัญชีแล้ว");
        } catch (ValidationException | ResourceNotFoundException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return REDIRECT;
    }

    private void populateList(Model model, Role role, Pageable pageable) {
        model.addAttribute("users", adminUserService.getUsers(role, pageable));
        model.addAttribute("roles", Role.values());
        model.addAttribute("selectedRole", role);
    }
}
