package com.smartlaundry.notificationservice.notification;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications/me")
    public List<NotificationService.NotificationResponse> myNotifications() {
        return notificationService.getMyNotifications();
    }

    @PostMapping("/subscriptions/devices/{id}")
    public NotificationService.SubscriptionResponse subscribe(@PathVariable UUID id) {
        return notificationService.subscribe(id);
    }
}
