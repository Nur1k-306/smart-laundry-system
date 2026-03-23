package com.smartlaundry.paymentservice.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "machine_id", nullable = false)
    private UUID machineId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static Payment create(UUID bookingId, UUID userId, UUID machineId, BigDecimal amount) {
        Payment payment = new Payment();
        payment.id = UUID.randomUUID();
        payment.bookingId = bookingId;
        payment.userId = userId;
        payment.machineId = machineId;
        payment.amount = amount;
        payment.status = PaymentStatus.PENDING;
        return payment;
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

    public UUID getBookingId() {
        return bookingId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getMachineId() {
        return machineId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void confirm() {
        this.status = PaymentStatus.CONFIRMED;
        this.rejectionReason = null;
    }

    public void reject(String reason) {
        this.status = PaymentStatus.REJECTED;
        this.rejectionReason = reason;
    }
}
