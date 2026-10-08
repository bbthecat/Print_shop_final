package com.printflow.service;

public interface NotificationService {

    void create(Long userId, Long orderId, String title, String message);

    long countUnread(Long userId);

    void markAsRead(Long notificationId, Long userId);
}