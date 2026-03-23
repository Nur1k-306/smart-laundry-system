package com.smartlaundry.notificationservice.notification;

import com.smartlaundry.common.events.EventPayloads.MachineStatusChangedPayload;
import com.smartlaundry.common.events.MachineBusinessStatus;
import com.smartlaundry.common.events.MachineTechnicalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private DeviceSubscriptionRepository subscriptionRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        subscriptionRepository = mock(DeviceSubscriptionRepository.class);
        notificationService = new NotificationService(notificationRepository, subscriptionRepository, mock(UserClient.class));
    }

    @Test
    void shouldNotifySubscribersWhenMachineBecomesFree() {
        DeviceSubscription subscription = DeviceSubscription.of(UUID.randomUUID(), UUID.randomUUID());
        when(subscriptionRepository.findAllByMachineIdAndActiveTrue(subscription.getMachineId())).thenReturn(List.of(subscription));

        notificationService.onMachineStatusChanged(new MachineStatusChangedPayload(
                subscription.getMachineId(),
                MachineBusinessStatus.FREE,
                MachineTechnicalStatus.OK,
                "wash-finished"
        ));

        verify(notificationRepository).save(any(NotificationDocument.class));
        verify(subscriptionRepository).save(subscription);
    }
}
