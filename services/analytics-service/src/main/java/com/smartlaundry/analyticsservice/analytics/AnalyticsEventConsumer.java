package com.smartlaundry.analyticsservice.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlaundry.common.events.DomainEvent;
import com.smartlaundry.common.events.EventType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsEventConsumer {

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    public AnalyticsEventConsumer(AnalyticsService analyticsService, ObjectMapper objectMapper) {
        this.analyticsService = analyticsService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = {
                    EventType.BOOKING_CREATED,
                    EventType.MACHINE_RESERVED,
                    EventType.PAYMENT_CREATED,
                    EventType.PAYMENT_CONFIRMED,
                    EventType.PAYMENT_REJECTED,
                    EventType.RESERVATION_EXPIRED,
                    EventType.WASH_STARTED,
                    EventType.WASH_FINISHED,
                    EventType.MACHINE_STATUS_CHANGED,
                    EventType.MACHINE_FAULT_DETECTED,
                    EventType.NOTIFICATION_REQUESTED
            },
            groupId = "analytics-service"
    )
    public void onEvent(String payload) throws JsonProcessingException {
        analyticsService.store(objectMapper.readValue(payload, DomainEvent.class));
    }
}
