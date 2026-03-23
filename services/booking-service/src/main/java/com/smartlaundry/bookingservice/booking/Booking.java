package com.smartlaundry.bookingservice.booking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "machine_id", nullable = false)
    private UUID machineId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static Booking create(UUID userId, UUID machineId, OffsetDateTime expiresAt) {
        Booking booking = new Booking();
        booking.id = UUID.randomUUID();
        booking.userId = userId;
        booking.machineId = machineId;
        booking.expiresAt = expiresAt;
        booking.status = BookingStatus.RESERVED;
        return booking;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getMachineId() {
        return machineId;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void markCancelled() {
        this.status = BookingStatus.CANCELLED;
    }

    public void markExpired() {
        this.status = BookingStatus.EXPIRED;
    }

    public void markPaid() {
        this.status = BookingStatus.PAID;
    }

    public void markPaymentRejected() {
        this.status = BookingStatus.PAYMENT_REJECTED;
    }
}
