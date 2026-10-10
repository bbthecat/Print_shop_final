package com.printflow.service;

import com.printflow.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void create(Long userId, Long orderId, String title, String message);

    Page<NotificationResponse> getMyNotifications(Long userId, Pageable pageable);

    long countUnread(Long userId);

    void markAsRead(Long notificationId, Long userId);
}