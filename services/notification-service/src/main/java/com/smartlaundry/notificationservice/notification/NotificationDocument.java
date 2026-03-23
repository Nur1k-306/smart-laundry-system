package com.smartlaundry.notificationservice.notification;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.UUID;

@Document(collection = "notifications")
public class NotificationDocument {

    @Id
    private String id;

    private UUID userId;
    private NotificationType type;
    private String title;
    private String message;
    private boolean read;
    private OffsetDateTime createdAt;

    public static NotificationDocument of(UUID userId, NotificationType type, String title, String message) {
        NotificationDocument notification = new NotificationDocument();
        notification.userId = userId;
        notification.type = type;
        notification.title = title;
        notification.message = message;
        notification.read = false;
        notification.createdAt = OffsetDateTime.now();
        return notification;
    }

    public String getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
