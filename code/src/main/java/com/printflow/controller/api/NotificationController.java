package com.printflow.controller.api;

import com.printflow.dto.response.NotificationResponse;
import com.printflow.security.CurrentUserProvider;
import com.printflow.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Notifications", description = "การแจ้งเตือนในระบบ")
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserProvider currentUserProvider;

    public NotificationController(
            NotificationService notificationService,
            CurrentUserProvider currentUserProvider
    ) {
        this.notificationService = notificationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @Operation(summary = "Get my notifications with pagination")
    public ResponseEntity<Page<NotificationResponse>> getMine(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                notificationService.getMyNotifications(
                        currentUserProvider.getCurrentUserId(),
                        pageable));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Count my unread notifications")
    public ResponseEntity<Long> countUnread() {
        return ResponseEntity.ok(
                notificationService.countUnread(currentUserProvider.getCurrentUserId()));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id, currentUserProvider.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}