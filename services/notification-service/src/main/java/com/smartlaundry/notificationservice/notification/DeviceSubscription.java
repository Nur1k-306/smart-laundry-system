package com.smartlaundry.notificationservice.notification;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.UUID;

@Document(collection = "device_subscriptions")
public class DeviceSubscription {

    @Id
    private String id;

    private UUID userId;
    private UUID machineId;
    private boolean active;
    private OffsetDateTime createdAt;

    public static DeviceSubscription of(UUID userId, UUID machineId) {
        DeviceSubscription subscription = new DeviceSubscription();
        subscription.userId = userId;
        subscription.machineId = machineId;
        subscription.active = true;
        subscription.createdAt = OffsetDateTime.now();
        return subscription;
    }

    public String getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getMachineId() {
        return machineId;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void deactivate() {
        this.active = false;
    }
}
