package com.smartlaundry.notificationservice.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlaundry.common.events.DomainEvent;
import com.smartlaundry.common.events.EventPayloads.MachineFaultDetectedPayload;
import com.smartlaundry.common.events.EventPayloads.MachineStatusChangedPayload;
import com.smartlaundry.common.events.EventPayloads.NotificationRequestedPayload;
import com.smartlaundry.common.events.EventPayloads.PaymentConfirmedPayload;
import com.smartlaundry.common.events.EventPayloads.PaymentRejectedPayload;
import com.smartlaundry.common.events.EventPayloads.ReservationExpiredPayload;
import com.smartlaundry.common.events.EventPayloads.WashFinishedPayload;
import com.smartlaundry.common.events.EventType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = EventType.RESERVATION_EXPIRED, groupId = "notification-service")
    public void onReservationExpired(String payload) throws JsonProcessingException {
        DomainEvent event = objectMapper.readValue(payload, DomainEvent.class);
        notificationService.onReservationExpired(event.payloadAs(objectMapper, ReservationExpiredPayload.class));
    }

    @KafkaListener(topics = EventType.PAYMENT_CONFIRMED, groupId = "notification-service")
    public void onPaymentConfirmed(String payload) throws JsonProcessingException {
        DomainEvent event = objectMapper.readValue(payload, DomainEvent.class);
        notificationService.onPaymentConfirmed(event.payloadAs(objectMapper, PaymentConfirmedPayload.class));
    }

    @KafkaListener(topics = EventType.PAYMENT_REJECTED, groupId = "notification-service")
    public void onPaymentRejected(String payload) throws JsonProcessingException {
        DomainEvent event = objectMapper.readValue(payload, DomainEvent.class);
        notificationService.onPaymentRejected(event.payloadAs(objectMapper, PaymentRejectedPayload.class));
    }

    @KafkaListener(topics = EventType.WASH_FINISHED, groupId = "notification-service")
    public void onWashFinished(String payload) throws JsonProcessingException {
        DomainEvent event = objectMapper.readValue(payload, DomainEvent.class);
        notificationService.onWashFinished(event.payloadAs(objectMapper, WashFinishedPayload.class));
    }

    @KafkaListener(topics = EventType.MACHINE_STATUS_CHANGED, groupId = "notification-service")
    public void onMachineStatusChanged(String payload) throws JsonProcessingException {
        DomainEvent event = objectMapper.readValue(payload, DomainEvent.class);
        notificationService.onMachineStatusChanged(event.payloadAs(objectMapper, MachineStatusChangedPayload.class));
    }

    @KafkaListener(topics = EventType.MACHINE_FAULT_DETECTED, groupId = "notification-service")
    public void onMachineFaultDetected(String payload) throws JsonProcessingException {
        DomainEvent event = objectMapper.readValue(payload, DomainEvent.class);
        notificationService.onMachineFaultDetected(event.payloadAs(objectMapper, MachineFaultDetectedPayload.class));
    }

    @KafkaListener(topics = EventType.NOTIFICATION_REQUESTED, groupId = "notification-service")
    public void onNotificationRequested(String payload) throws JsonProcessingException {
        DomainEvent event = objectMapper.readValue(payload, DomainEvent.class);
        notificationService.onNotificationRequested(event.payloadAs(objectMapper, NotificationRequestedPayload.class));
    }
}
