package com.smartlaundry.deviceservice.machine;

import com.smartlaundry.common.events.MachineBusinessStatus;
import com.smartlaundry.common.events.MachineTechnicalStatus;
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
@Table(name = "machines")
public class Machine {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_status", nullable = false)
    private MachineBusinessStatus businessStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "technical_status", nullable = false)
    private MachineTechnicalStatus technicalStatus;

    @Column(name = "active_booking_id")
    private UUID activeBookingId;

    @Column(name = "active_user_id")
    private UUID activeUserId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static Machine create(String name, String location) {
        Machine machine = new Machine();
        machine.id = UUID.randomUUID();
        machine.name = name;
        machine.location = location;
        machine.businessStatus = MachineBusinessStatus.FREE;
        machine.technicalStatus = MachineTechnicalStatus.OK;
        return machine;
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

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public MachineBusinessStatus getBusinessStatus() {
        return businessStatus;
    }

    public MachineTechnicalStatus getTechnicalStatus() {
        return technicalStatus;
    }

    public UUID getActiveBookingId() {
        return activeBookingId;
    }

    public UUID getActiveUserId() {
        return activeUserId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void reserve(UUID bookingId, UUID userId) {
        this.businessStatus = MachineBusinessStatus.RESERVED;
        this.activeBookingId = bookingId;
        this.activeUserId = userId;
    }

    public void markBusy(UUID bookingId, UUID userId) {
        this.businessStatus = MachineBusinessStatus.BUSY;
        this.activeBookingId = bookingId;
        this.activeUserId = userId;
    }

    public void free() {
        this.businessStatus = MachineBusinessStatus.FREE;
        this.activeBookingId = null;
        this.activeUserId = null;
    }

    public void updateTechnicalStatus(MachineTechnicalStatus technicalStatus) {
        this.technicalStatus = technicalStatus;
    }
}
