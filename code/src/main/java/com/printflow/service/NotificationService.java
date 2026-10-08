package com.printflow.service;

public interface NotificationService {

    long countUnread(Long userId);

    void markAsRead(Long notificationId, Long userId);
}