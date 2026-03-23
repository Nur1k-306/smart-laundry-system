package com.smartlaundry.common.events;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class EventPayloads {

    private EventPayloads() {
    }

    public record BookingCreatedPayload(
            UUID bookingId,
            UUID userId,
            UUID machineId,
            OffsetDateTime expiresAt
    ) {
    }

    public record MachineReservedPayload(
            UUID machineId,
            UUID bookingId,
            UUID userId,
            MachineBusinessStatus businessStatus,
            MachineTechnicalStatus technicalStatus
    ) {
    }

    public record PaymentCreatedPayload(
            UUID paymentId,
            UUID bookingId,
            UUID userId,
            UUID machineId,
            BigDecimal amount,
            String status
    ) {
    }

    public record PaymentConfirmedPayload(
            UUID paymentId,
            UUID bookingId,
            UUID userId,
            UUID machineId,
            BigDecimal amount
    ) {
    }

    public record PaymentRejectedPayload(
            UUID paymentId,
            UUID bookingId,
            UUID userId,
            UUID machineId,
            String reason
    ) {
    }

    public record ReservationExpiredPayload(
            UUID bookingId,
            UUID userId,
            UUID machineId,
            String reason
    ) {
    }

    public record WashStartedPayload(
            String washSessionId,
            UUID bookingId,
            UUID paymentId,
            UUID userId,
            UUID machineId,
            OffsetDateTime startedAt,
            OffsetDateTime expectedEndAt
    ) {
    }

    public record WashFinishedPayload(
            String washSessionId,
            UUID bookingId,
            UUID userId,
            UUID machineId,
            OffsetDateTime finishedAt
    ) {
    }

    public record MachineStatusChangedPayload(
            UUID machineId,
            MachineBusinessStatus businessStatus,
            MachineTechnicalStatus technicalStatus,
            String reason
    ) {
    }

    public record MachineFaultDetectedPayload(
            UUID machineId,
            String description
    ) {
    }

    public record NotificationRequestedPayload(
            UUID userId,
            String title,
            String message,
            String type
    ) {
    }
}
