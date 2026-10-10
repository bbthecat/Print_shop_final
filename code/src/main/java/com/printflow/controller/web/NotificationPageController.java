package com.printflow.controller.web;

import com.printflow.security.CurrentUserProvider;
import com.printflow.service.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notifications")
public class NotificationPageController {

    private final NotificationService notificationService;
    private final CurrentUserProvider currentUserProvider;

    public NotificationPageController(
            NotificationService notificationService,
            CurrentUserProvider currentUserProvider
    ) {
        this.notificationService = notificationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public String list(
            @PageableDefault(size = 20) Pageable pageable,
            Model model
    ) {
        Long userId = currentUserProvider.getCurrentUserId();
        model.addAttribute("notifications", notificationService.getMyNotifications(userId, pageable));
        model.addAttribute("unreadCount", notificationService.countUnread(userId));
        return "notifications/index";
    }

    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id, currentUserProvider.getCurrentUserId());
        return "redirect:/notifications";
    }
}