package com.printflow.controller.web.staff;

import com.printflow.domain.enums.PaymentMethod;
import com.printflow.dto.response.OrderResponse;
import com.printflow.dto.response.PaymentResponse;
import com.printflow.exception.InvalidStateTransitionException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.mapper.PaymentMapper;
import com.printflow.service.OrderQueryService;
import com.printflow.service.PaymentService;
import org.springframework.data.domain.Page;
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

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/staff/payments")
public class StaffPaymentPageController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;
    private final OrderQueryService orderQueryService;

    public StaffPaymentPageController(
            PaymentService paymentService,
            PaymentMapper paymentMapper,
            OrderQueryService orderQueryService
    ) {
        this.paymentService = paymentService;
        this.paymentMapper = paymentMapper;
        this.orderQueryService = orderQueryService;
    }

    @GetMapping
    public String list(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<PaymentResponse> payments = paymentService.findAll(pageable).map(paymentMapper::toResponse);

        // ดึง order ของแต่ละรายการ เพื่อแสดงเลขคำสั่งซื้อและสถานะ (หน้าละ 10 รายการ)
        Map<Long, OrderResponse> orders = new HashMap<>();
        for (PaymentResponse payment : payments) {
            orders.put(payment.orderId(), orderQueryService.getById(payment.orderId()));
        }

        model.addAttribute("payments", payments);
        model.addAttribute("orders", orders);
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
