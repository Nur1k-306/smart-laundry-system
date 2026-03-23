package com.smartlaundry.analyticsservice.analytics;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;

@Document(collection = "event_audit")
public class EventAuditDocument {

    @Id
    private String id;

    private String eventId;
    private String eventType;
    private OffsetDateTime occurredAt;
    private String correlationId;
    private String payload;

    public static EventAuditDocument of(String eventId, String eventType, OffsetDateTime occurredAt, String correlationId, String payload) {
        EventAuditDocument document = new EventAuditDocument();
        document.eventId = eventId;
        document.eventType = eventType;
        document.occurredAt = occurredAt;
        document.correlationId = correlationId;
        document.payload = payload;
        return document;
    }

    public String getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getPayload() {
        return payload;
    }
}
