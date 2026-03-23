package com.smartlaundry.paymentservice.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlaundry.common.events.DomainEvent;
import com.smartlaundry.common.events.EventPayloads.ReservationExpiredPayload;
import com.smartlaundry.common.events.EventType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public PaymentEventConsumer(PaymentService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = EventType.RESERVATION_EXPIRED, groupId = "payment-service")
    public void onReservationExpired(String payload) throws JsonProcessingException {
        DomainEvent event = objectMapper.readValue(payload, DomainEvent.class);
        paymentService.onReservationExpired(event.payloadAs(objectMapper, ReservationExpiredPayload.class));
    }
}
