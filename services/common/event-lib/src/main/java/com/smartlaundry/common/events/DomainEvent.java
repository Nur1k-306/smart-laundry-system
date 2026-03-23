package com.smartlaundry.common.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DomainEvent(
        UUID eventId,
        String eventType,
        OffsetDateTime occurredAt,
        String correlationId,
        JsonNode payload
) {

    public static DomainEvent of(String eventType, String correlationId, Object payload, ObjectMapper objectMapper) {
        return new DomainEvent(
                UUID.randomUUID(),
                eventType,
                OffsetDateTime.now(),
                correlationId,
                objectMapper.valueToTree(payload)
        );
    }

    public <T> T payloadAs(ObjectMapper objectMapper, Class<T> payloadType) {
        return objectMapper.convertValue(payload, payloadType);
    }
}
