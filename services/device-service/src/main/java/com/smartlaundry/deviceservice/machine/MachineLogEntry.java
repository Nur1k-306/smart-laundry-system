package com.smartlaundry.deviceservice.machine;

import com.smartlaundry.common.events.MachineBusinessStatus;
import com.smartlaundry.common.events.MachineTechnicalStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.UUID;

@Document(collection = "machine_logs")
public class MachineLogEntry {

    @Id
    private String id;

    private UUID machineId;
    private MachineBusinessStatus businessStatus;
    private MachineTechnicalStatus technicalStatus;
    private String reason;
    private OffsetDateTime createdAt;

    public static MachineLogEntry of(Machine machine, String reason) {
        MachineLogEntry entry = new MachineLogEntry();
        entry.machineId = machine.getId();
        entry.businessStatus = machine.getBusinessStatus();
        entry.technicalStatus = machine.getTechnicalStatus();
        entry.reason = reason;
        entry.createdAt = OffsetDateTime.now();
        return entry;
    }

    public String getId() {
        return id;
    }

    public UUID getMachineId() {
        return machineId;
    }

    public MachineBusinessStatus getBusinessStatus() {
        return businessStatus;
    }

    public MachineTechnicalStatus getTechnicalStatus() {
        return technicalStatus;
    }

    public String getReason() {
        return reason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
