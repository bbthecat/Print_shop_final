package com.printflow.controller.web.staff;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.exception.InvalidStateTransitionException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.OrderQueryService;
import com.printflow.service.OrderStatusService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/orders/{orderId}")
public class StaffOrderDetailPageController {

    private final OrderQueryService orderQueryService;
    private final OrderStatusService orderStatusService;
    private final CurrentUserProvider currentUserProvider;

    public StaffOrderDetailPageController(
            OrderQueryService orderQueryService,
            OrderStatusService orderStatusService,
            CurrentUserProvider currentUserProvider
    ) {
        this.orderQueryService = orderQueryService;
        this.orderStatusService = orderStatusService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public String detail(@PathVariable Long orderId, Model model) {
        model.addAttribute("order", orderQueryService.getById(orderId));
        model.addAttribute("histories", orderStatusService.getHistories(orderId));
        model.addAttribute("statuses", OrderStatus.values());
        return "staff/order-detail";
    }

    @PostMapping("/status")
    public String changeStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            RedirectAttributes redirect
    ) {
        try {
            orderStatusService.changeStatus(orderId, status, currentUserProvider.getCurrentUserId());
            redirect.addFlashAttribute("success", "เปลี่ยนสถานะเป็น " + status + " แล้ว");
        } catch (InvalidStateTransitionException | ResourceNotFoundException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/orders/" + orderId;
    }
}