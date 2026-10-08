package com.printflow.controller.web.staff;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.service.OrderQueryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/staff/orders")
public class StaffOrderPageController {

    private final OrderQueryService orderQueryService;

    public StaffOrderPageController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        model.addAttribute("orders", status == null
                ? orderQueryService.getAll(pageable)
                : orderQueryService.getAllByStatus(status, pageable));
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        return "staff/orders";
    }
}