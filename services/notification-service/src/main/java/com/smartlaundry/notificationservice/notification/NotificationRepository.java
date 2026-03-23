package com.smartlaundry.notificationservice.notification;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends MongoRepository<NotificationDocument, String> {

    List<NotificationDocument> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
