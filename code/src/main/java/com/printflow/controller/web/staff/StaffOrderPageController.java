package com.printflow.controller.web.staff;

import com.printflow.domain.enums.OrderStatus;
import com.printflow.dto.response.OrderResponse;
import com.printflow.exception.InvalidStateTransitionException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.OrderQueryService;
import com.printflow.service.OrderStatusService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/staff/orders")
public class StaffOrderPageController {

    private final OrderQueryService orderQueryService;
    private final OrderStatusService orderStatusService;
    private final CurrentUserProvider currentUserProvider;

    public StaffOrderPageController(OrderQueryService orderQueryService,
                                    OrderStatusService orderStatusService,
                                    CurrentUserProvider currentUserProvider) {
        this.orderQueryService = orderQueryService;
        this.orderStatusService = orderStatusService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<OrderResponse> orders = status == null
                ? orderQueryService.getAll(pageable)
                : orderQueryService.getAllByStatus(status, pageable);

        // ขั้นถัดไปของแต่ละ order (ไม่มี = ส่งมอบแล้ว/ยกเลิกแล้ว ติ๊กไม่ได้)
        Map<Long, OrderStatus> nextStatusById = new HashMap<>();
        for (OrderResponse order : orders.getContent()) {
            orderStatusService.getNextStatus(order.status())
                    .ifPresent(next -> nextStatusById.put(order.id(), next));
        }

        model.addAttribute("orders", orders);
        model.addAttribute("nextStatusById", nextStatusById);
        // จำนวนงานในแต่ละสถานะ สำหรับแท็บด้านบน
        Map<OrderStatus, Long> statusCounts = new EnumMap<>(OrderStatus.class);
        long totalCount = 0;
        for (OrderStatus s : OrderStatus.values()) {
            long count = orderQueryService.countByStatus(s);
            statusCounts.put(s, count);
            totalCount += count;
        }
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        return "staff/orders";
    }

    // เลื่อนหลาย order ไปขั้นถัดไปของแต่ละตัว ทีละ order (ตัวที่เปลี่ยนไม่ได้ข้ามไป ไม่ยกเลิกทั้งชุด)
    @PostMapping("/advance")
    public String advance(
            @RequestParam(required = false) List<Long> orderIds,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            RedirectAttributes redirect
    ) {
        String back = UriComponentsBuilder.fromPath("/staff/orders")
                .queryParamIfPresent("status", Optional.ofNullable(status))
                .queryParam("page", Math.max(page, 0))
                .toUriString();

        if (orderIds == null || orderIds.isEmpty()) {
            redirect.addFlashAttribute("error", "กรุณาเลือกคำสั่งซื้ออย่างน้อย 1 รายการ");
            return "redirect:" + back;
        }

        Long actorId = currentUserProvider.getCurrentUserId();
        int done = 0;
        List<String> skipped = new ArrayList<>();
        for (Long orderId : orderIds.stream().distinct().toList()) {
            String label = "#" + orderId;
            try {
                OrderResponse order = orderQueryService.getById(orderId);
                label = order.orderNumber();
                Optional<OrderStatus> next = orderStatusService.getNextStatus(order.status());
                if (next.isEmpty()) {
                    skipped.add(label);
                    continue;
                }
                orderStatusService.changeStatus(orderId, next.get(), actorId);
                done++;
            } catch (InvalidStateTransitionException | ResourceNotFoundException ex) {
                skipped.add(label);
            }
        }

        if (done > 0) {
            redirect.addFlashAttribute("success", "เลื่อนสถานะสำเร็จ " + done + " รายการ");
        }
        if (!skipped.isEmpty()) {
            redirect.addFlashAttribute("error",
                    "เลื่อนสถานะไม่ได้ " + skipped.size() + " รายการ: " + String.join(", ", skipped));
        }
        return "redirect:" + back;
    }
}
