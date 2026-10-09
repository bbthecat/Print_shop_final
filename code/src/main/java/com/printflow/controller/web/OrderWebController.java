package com.printflow.controller.web;

import com.printflow.dto.form.OrderCreateForm;
import com.printflow.dto.request.OrderCreateRequest;
import com.printflow.dto.request.OrderItemRequest;
import com.printflow.dto.response.OrderResponse;
import com.printflow.exception.InvalidStateTransitionException;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.exception.ValidationException;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.OrderCommandService;
import com.printflow.service.OrderQueryService;
import com.printflow.service.OrderStatusService;
import com.printflow.service.PromotionService;
import com.printflow.service.ServiceCatalogQueryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderWebController {

    private static final int MAX_PAGE_SIZE = 50;

    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;
    private final OrderStatusService orderStatusService;
    private final ServiceCatalogQueryService catalogQueryService;
    private final PromotionService promotionService;
    private final CurrentUserProvider currentUserProvider;

    // Constructor Injection ตามเกณฑ์ห้าม @Autowired บน field
    // Controller เรียก Service เท่านั้น ห้ามเรียก Repository ตรง
    public OrderWebController(
            OrderQueryService orderQueryService,
            OrderCommandService orderCommandService,
            OrderStatusService orderStatusService,
            ServiceCatalogQueryService catalogQueryService,
            PromotionService promotionService,
            CurrentUserProvider currentUserProvider
    ) {
        this.orderQueryService = orderQueryService;
        this.orderCommandService = orderCommandService;
        this.orderStatusService = orderStatusService;
        this.catalogQueryService = catalogQueryService;
        this.promotionService = promotionService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        populateCatalog(model);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new OrderCreateForm());
        }
        return "orders/create";
    }

    @PostMapping("/create")
    public String createOrder(
            @Valid @ModelAttribute("form") OrderCreateForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirect
    ) {
        if (bindingResult.hasErrors()) {
            populateCatalog(model);
            return "orders/create";
        }

        try {
            Long userId = currentUserProvider.getCurrentUserId();

            OrderItemRequest itemRequest = new OrderItemRequest(
                    form.getServiceId(),
                    form.getPageCount(),
                    form.getQuantity(),
                    form.getAddonIds() != null ? form.getAddonIds() : List.of()
            );

            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(itemRequest),
                    form.getPromotionCode() != null && !form.getPromotionCode().isBlank()
                            ? form.getPromotionCode().trim()
                            : null,
                    form.getFileName(),
                    form.getFileUrl()
            );

            OrderResponse createdOrder = orderCommandService.createOrder(userId, request);
            redirect.addFlashAttribute("success", "สร้างคำสั่งซื้อ " + createdOrder.orderNumber() + " สำเร็จแล้ว!");
            return "redirect:/orders/" + createdOrder.id();

        } catch (ValidationException | ResourceNotFoundException ex) {
            // แสดงฟอร์มเดิมพร้อมข้อความ error ข้อมูลที่กรอกไว้จะไม่หาย
            populateCatalog(model);
            model.addAttribute("error", ex.getMessage());
            return "orders/create";
        }
    }

    @GetMapping
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean all,
            Model model
    ) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by("createdAt").descending());
        Long userId = currentUserProvider.getCurrentUserId();

        boolean isStaffOrAdmin = currentUserProvider.isStaffOrAdmin();

        Page<OrderResponse> orders;
        if (Boolean.TRUE.equals(all) && isStaffOrAdmin) {
            orders = orderQueryService.getAll(pageable);
            model.addAttribute("viewAll", true);
        } else {
            orders = orderQueryService.getAllByUserId(userId, pageable);
            model.addAttribute("viewAll", false);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("isStaffOrAdmin", isStaffOrAdmin);
        return "orders/history";
    }

    @GetMapping("/{id}")
    public String getOrderDetail(@PathVariable Long id, Model model) {
        OrderResponse order = findVisibleOrder(id);
        model.addAttribute("order", order);
        model.addAttribute("files", orderQueryService.getFiles(id));
        return "orders/detail";
    }

    @GetMapping("/{id}/tracking")
    public String getOrderTracking(@PathVariable Long id, Model model) {
        OrderResponse order = findVisibleOrder(id);
        model.addAttribute("order", order);
        return "orders/tracking";
    }

    // UC-07: ลูกค้ายกเลิก order ของตัวเองได้ตอนที่ยังรอร้านยืนยัน
    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            orderStatusService.cancelByCustomer(id, currentUserProvider.getCurrentUserId());
            redirect.addFlashAttribute("success", "ยกเลิกคำสั่งซื้อเรียบร้อยแล้ว");
        } catch (InvalidStateTransitionException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    // ลูกค้าดูได้เฉพาะ order ของตัวเอง STAFF/ADMIN ดูได้ทุก order
    private OrderResponse findVisibleOrder(Long id) {
        return orderQueryService.getByIdForUser(
                id,
                currentUserProvider.getCurrentUserId(),
                currentUserProvider.isStaffOrAdmin()
        );
    }

    private void populateCatalog(Model model) {
        model.addAttribute("services", catalogQueryService.findAllActivePrintServices());
        model.addAttribute("addons", catalogQueryService.findAllActiveAddonServices());
        model.addAttribute("promotions", promotionService.findAllActive());
    }
}
