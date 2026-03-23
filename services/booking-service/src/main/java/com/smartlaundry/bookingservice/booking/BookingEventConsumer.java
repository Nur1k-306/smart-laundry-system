package com.smartlaundry.bookingservice.booking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlaundry.common.events.DomainEvent;
import com.smartlaundry.common.events.EventPayloads.PaymentConfirmedPayload;
import com.smartlaundry.common.events.EventPayloads.PaymentRejectedPayload;
import com.smartlaundry.common.events.EventType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventConsumer {

    private final BookingService bookingService;
    private final ObjectMapper objectMapper;

    public BookingEventConsumer(BookingService bookingService, ObjectMapper objectMapper) {
        this.bookingService = bookingService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = EventType.PAYMENT_CONFIRMED, groupId = "booking-service")
    public void onPaymentConfirmed(String payload) throws JsonProcessingException {
        DomainEvent event = objectMapper.readValue(payload, DomainEvent.class);
        bookingService.onPaymentConfirmed(event.payloadAs(objectMapper, PaymentConfirmedPayload.class));
    }

    @KafkaListener(topics = EventType.PAYMENT_REJECTED, groupId = "booking-service")
    public void onPaymentRejected(String payload) throws JsonProcessingException {
        DomainEvent event = objectMapper.readValue(payload, DomainEvent.class);
        bookingService.onPaymentRejected(event.payloadAs(objectMapper, PaymentRejectedPayload.class));
    }
}
