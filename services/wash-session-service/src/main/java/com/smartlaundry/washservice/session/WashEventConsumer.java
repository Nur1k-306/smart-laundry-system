package com.smartlaundry.washservice.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlaundry.common.events.DomainEvent;
import com.smartlaundry.common.events.EventPayloads.PaymentConfirmedPayload;
import com.smartlaundry.common.events.EventType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class WashEventConsumer {

    private final WashSessionService washSessionService;
    private final ObjectMapper objectMapper;

    public WashEventConsumer(WashSessionService washSessionService, ObjectMapper objectMapper) {
        this.washSessionService = washSessionService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = EventType.PAYMENT_CONFIRMED, groupId = "wash-session-service")
    public void onPaymentConfirmed(String payload) throws JsonProcessingException {
        DomainEvent event = objectMapper.readValue(payload, DomainEvent.class);
        washSessionService.onPaymentConfirmed(event.payloadAs(objectMapper, PaymentConfirmedPayload.class));
    }
}
