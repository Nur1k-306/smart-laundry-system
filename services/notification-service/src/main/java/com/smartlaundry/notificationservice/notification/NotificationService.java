package com.smartlaundry.notificationservice.notification;

import com.smartlaundry.common.events.EventPayloads.MachineFaultDetectedPayload;
import com.smartlaundry.common.events.EventPayloads.MachineStatusChangedPayload;
import com.smartlaundry.common.events.EventPayloads.NotificationRequestedPayload;
import com.smartlaundry.common.events.EventPayloads.PaymentConfirmedPayload;
import com.smartlaundry.common.events.EventPayloads.PaymentRejectedPayload;
import com.smartlaundry.common.events.EventPayloads.ReservationExpiredPayload;
import com.smartlaundry.common.events.EventPayloads.WashFinishedPayload;
import com.smartlaundry.common.events.MachineBusinessStatus;
import com.smartlaundry.common.security.Role;
import com.smartlaundry.common.web.ConflictException;
import com.smartlaundry.common.web.RoleGuard;
import com.smartlaundry.common.web.UserContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final DeviceSubscriptionRepository subscriptionRepository;
    private final UserClient userClient;

    public NotificationService(
            NotificationRepository notificationRepository,
            DeviceSubscriptionRepository subscriptionRepository,
            UserClient userClient
    ) {
        this.notificationRepository = notificationRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userClient = userClient;
    }

    public List<NotificationResponse> getMyNotifications() {
        UserContext user = RoleGuard.requireAuthenticated();
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.userId()).stream().map(NotificationResponse::from).toList();
    }

    public SubscriptionResponse subscribe(UUID machineId) {
        UserContext user = RoleGuard.requireAuthenticated();
        subscriptionRepository.findByUserIdAndMachineIdAndActiveTrue(user.userId(), machineId).ifPresent(existing -> {
            throw new ConflictException("Подписка уже оформлена");
        });

        DeviceSubscription subscription = subscriptionRepository.save(DeviceSubscription.of(user.userId(), machineId));
        return new SubscriptionResponse(subscription.getId(), subscription.getUserId(), subscription.getMachineId(), subscription.isActive());
    }

    public void onReservationExpired(ReservationExpiredPayload payload) {
        save(payload.userId(), NotificationType.INFO, "Бронь истекла", "Бронь на машину " + payload.machineId() + " истекла.");
    }

    public void onPaymentConfirmed(PaymentConfirmedPayload payload) {
        save(payload.userId(), NotificationType.PAYMENT, "Оплата подтверждена", "Оплата подтверждена, стирка машины " + payload.machineId() + " началась.");
    }

    public void onPaymentRejected(PaymentRejectedPayload payload) {
        save(payload.userId(), NotificationType.PAYMENT, "Оплата отклонена", payload.reason());
    }

    public void onWashFinished(WashFinishedPayload payload) {
        save(payload.userId(), NotificationType.WASH, "Стирка завершена", "Стирка машины " + payload.machineId() + " завершена.");
    }

    public void onMachineStatusChanged(MachineStatusChangedPayload payload) {
        if (payload.businessStatus() != MachineBusinessStatus.FREE) {
            return;
        }

        List<DeviceSubscription> subscriptions = subscriptionRepository.findAllByMachineIdAndActiveTrue(payload.machineId());
        subscriptions.forEach(subscription -> {
            save(subscription.getUserId(), NotificationType.MACHINE, "Машина свободна", "Машина " + payload.machineId() + " снова свободна.");
            subscription.deactivate();
            subscriptionRepository.save(subscription);
        });
    }

    public void onMachineFaultDetected(MachineFaultDetectedPayload payload) {
        userClient.getUsersByRole(Role.ADMIN)
                .forEach(admin -> save(admin.id(), NotificationType.ALERT, "Обнаружена неисправность машины", payload.description()));
    }

    public void onNotificationRequested(NotificationRequestedPayload payload) {
        save(payload.userId(), NotificationType.valueOf(payload.type()), payload.title(), payload.message());
    }

    private void save(UUID userId, NotificationType type, String title, String message) {
        notificationRepository.save(NotificationDocument.of(userId, type, title, message));
    }

    public record NotificationResponse(
            String id,
            UUID userId,
            NotificationType type,
            String title,
            String message,
            boolean read
    ) {
        public static NotificationResponse from(NotificationDocument document) {
            return new NotificationResponse(
                    document.getId(),
                    document.getUserId(),
                    document.getType(),
                    document.getTitle(),
                    document.getMessage(),
                    document.isRead()
            );
        }
    }

    public record SubscriptionResponse(
            String id,
            UUID userId,
            UUID machineId,
            boolean active
    ) {
    }
}
