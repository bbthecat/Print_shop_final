package com.printflow.service.impl;

import com.printflow.domain.entity.Notification;
import com.printflow.exception.ResourceNotFoundException;
import com.printflow.repository.NotificationRepository;
import com.printflow.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void create(Long userId, Long orderId, String title, String message) {
        notificationRepository.save(new Notification(userId, orderId, title, message));
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found: " + notificationId));

        notification.markAsRead();
        notificationRepository.save(notification);
    }
}