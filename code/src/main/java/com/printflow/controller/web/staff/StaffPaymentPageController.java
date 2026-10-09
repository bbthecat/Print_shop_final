package com.printflow.controller.web.staff;

import com.printflow.domain.enums.PaymentMethod;
import com.printflow.exception.InvalidStateTransitionException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.service.PaymentService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/payments")
public class StaffPaymentPageController {

    private final PaymentService paymentService;

    public StaffPaymentPageController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public String list(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        model.addAttribute("payments", paymentService.findAll(pageable));
        model.addAttribute("methods", PaymentMethod.values());
        return "staff/payments";
    }

    @PostMapping("/{orderId}/paid")
    public String markAsPaid(
            @PathVariable Long orderId,
            @RequestParam PaymentMethod method,
            RedirectAttributes redirect
    ) {
        try {
            paymentService.markAsPaid(orderId, method);
            redirect.addFlashAttribute("success", "บันทึกการชำระเงินแล้ว");
        } catch (ResourceNotFoundException | InvalidStateTransitionException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/payments";
    }
}