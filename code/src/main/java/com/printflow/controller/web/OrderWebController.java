package com.printflow.controller.web;

import com.printflow.dto.form.OrderCreateForm;
import com.printflow.dto.request.OrderCreateRequest;
import com.printflow.dto.request.OrderItemRequest;
import com.printflow.dto.response.OrderResponse;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.exception.ValidationException;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.OrderCommandService;
import com.printflow.service.OrderQueryService;
import com.printflow.service.PromotionService;
import com.printflow.service.ServiceCatalogQueryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderWebController {

    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;
    private final ServiceCatalogQueryService catalogQueryService;
    private final PromotionService promotionService;
    private final CurrentUserProvider currentUserProvider;

    // Constructor Injection ตามเกณฑ์ห้าม @Autowired บน field
    // Controller เรียก Service เท่านั้น ห้ามเรียก Repository ตรง
    public OrderWebController(
            OrderQueryService orderQueryService,
            OrderCommandService orderCommandService,
            ServiceCatalogQueryService catalogQueryService,
            PromotionService promotionService,
            CurrentUserProvider currentUserProvider
    ) {
        this.orderQueryService = orderQueryService;
        this.orderCommandService = orderCommandService;
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
                    form.getQuantity(),
                    form.getAddonIds() != null ? form.getAddonIds() : List.of()
            );

            OrderCreateRequest request = new OrderCreateRequest(
                    userId,
                    List.of(itemRequest),
                    form.getPromotionCode() != null && !form.getPromotionCode().isBlank()
                            ? form.getPromotionCode().trim()
                            : null
            );

            OrderResponse createdOrder = orderCommandService.createOrder(request);
            redirect.addFlashAttribute("success", "สร้างคำสั่งซื้อ " + createdOrder.orderNumber() + " สำเร็จแล้ว!");
            return "redirect:/orders/" + createdOrder.id();

        } catch (ValidationException | ResourceNotFoundException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/orders/create";
        }
    }

    @GetMapping
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean all,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Long userId = currentUserProvider.getCurrentUserId();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isStaffOrAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF")
        );

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
        OrderResponse order = orderQueryService.getById(id);
        model.addAttribute("order", order);
        return "orders/detail";
    }

    @GetMapping("/{id}/tracking")
    public String getOrderTracking(@PathVariable Long id, Model model) {
        OrderResponse order = orderQueryService.getById(id);
        model.addAttribute("order", order);
        return "orders/tracking";
    }

    private void populateCatalog(Model model) {
        model.addAttribute("services", catalogQueryService.findAllActivePrintServices());
        model.addAttribute("addons", catalogQueryService.findAllActiveAddonServices());
        model.addAttribute("promotions", promotionService.findAllActive());
    }
}
