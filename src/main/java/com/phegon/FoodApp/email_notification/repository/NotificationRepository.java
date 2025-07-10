package com.phegon.FoodApp.email_notification.repository;

import com.phegon.FoodApp.email_notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Additional query methods can be defined here if needed
    // For example, to find notifications by recipient or type
    // List<Notification> findByRecipient(String recipient);
    // List<Notification> findByType(NotificationType type);
}
