package com.smartlaundry.washservice.session;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.UUID;

@Document(collection = "wash_sessions")
public class WashSession {

    @Id
    private String id;

    private UUID bookingId;
    private UUID paymentId;
    private UUID userId;
    private UUID machineId;
    private WashSessionStatus status;
    private OffsetDateTime startedAt;
    private OffsetDateTime expectedEndAt;
    private OffsetDateTime finishedAt;

    public static WashSession start(UUID bookingId, UUID paymentId, UUID userId, UUID machineId, long durationSeconds) {
        WashSession session = new WashSession();
        session.bookingId = bookingId;
        session.paymentId = paymentId;
        session.userId = userId;
        session.machineId = machineId;
        session.status = WashSessionStatus.RUNNING;
        session.startedAt = OffsetDateTime.now();
        session.expectedEndAt = session.startedAt.plusSeconds(durationSeconds);
        return session;
    }

    public String getId() {
        return id;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getMachineId() {
        return machineId;
    }

    public WashSessionStatus getStatus() {
        return status;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getExpectedEndAt() {
        return expectedEndAt;
    }

    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }

    public void finish() {
        this.status = WashSessionStatus.FINISHED;
        this.finishedAt = OffsetDateTime.now();
    }
}
