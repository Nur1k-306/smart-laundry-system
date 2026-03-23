package com.smartlaundry.deviceservice.machine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlaundry.common.events.DomainEvent;
import com.smartlaundry.common.events.EventPayloads.MachineFaultDetectedPayload;
import com.smartlaundry.common.events.EventPayloads.MachineReservedPayload;
import com.smartlaundry.common.events.EventPayloads.MachineStatusChangedPayload;
import com.smartlaundry.common.events.EventPayloads.ReservationExpiredPayload;
import com.smartlaundry.common.events.EventPayloads.WashFinishedPayload;
import com.smartlaundry.common.events.EventPayloads.WashStartedPayload;
import com.smartlaundry.common.events.EventType;
import com.smartlaundry.common.events.MachineBusinessStatus;
import com.smartlaundry.common.events.MachineTechnicalStatus;
import com.smartlaundry.common.web.BadRequestException;
import com.smartlaundry.common.web.ConflictException;
import com.smartlaundry.common.web.CorrelationIdHolder;
import com.smartlaundry.common.web.NotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MachineService {

    private final MachineRepository repository;
    private final MachineLogRepository logRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public MachineService(
            MachineRepository repository,
            MachineLogRepository logRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.logRepository = logRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Cacheable("devices")
    @Transactional(readOnly = true)
    public List<MachineResponse> getAll() {
        return repository.findAll().stream().map(MachineResponse::from).toList();
    }

    @Cacheable("freeDevices")
    @Transactional(readOnly = true)
    public List<MachineResponse> getFree() {
        return repository.findAllByBusinessStatus(MachineBusinessStatus.FREE).stream().map(MachineResponse::from).toList();
    }

    @Cacheable(value = "deviceById", key = "#id")
    @Transactional(readOnly = true)
    public MachineResponse getById(UUID id) {
        return MachineResponse.from(findMachine(id));
    }

    @Transactional(readOnly = true)
    public List<MachineResponse> getFaults() {
        return repository.findAllByTechnicalStatus(MachineTechnicalStatus.ERROR).stream().map(MachineResponse::from).toList();
    }

    @Transactional
    @CacheEvict(value = {"devices", "freeDevices", "deviceById"}, allEntries = true)
    public MachineResponse updateTechnicalStatus(UUID id, MachineTechnicalStatus status) {
        Machine machine = repository.findLockedById(id).orElseThrow(() -> new NotFoundException("Машина не найдена"));
        machine.updateTechnicalStatus(status);
        logRepository.save(MachineLogEntry.of(machine, "technical-status-updated"));
        repository.save(machine);

        publish(EventType.MACHINE_STATUS_CHANGED, new MachineStatusChangedPayload(
                machine.getId(),
                machine.getBusinessStatus(),
                machine.getTechnicalStatus(),
                "technical-status-updated"
        ));

        if (status == MachineTechnicalStatus.ERROR) {
            publish(EventType.MACHINE_FAULT_DETECTED, new MachineFaultDetectedPayload(machine.getId(), "Администратор отметил машину как неисправную"));
        }
        return MachineResponse.from(machine);
    }

    @Transactional
    @CacheEvict(value = {"devices", "freeDevices", "deviceById"}, allEntries = true)
    public MachineResponse reserveMachine(UUID machineId, UUID bookingId, UUID userId) {
        Machine machine = repository.findLockedById(machineId).orElseThrow(() -> new NotFoundException("Машина не найдена"));
        if (machine.getTechnicalStatus() == MachineTechnicalStatus.ERROR) {
            throw new BadRequestException("Машина в состоянии ERROR");
        }
        if (machine.getBusinessStatus() != MachineBusinessStatus.FREE) {
            throw new ConflictException("Машина сейчас не свободна");
        }

        machine.reserve(bookingId, userId);
        repository.save(machine);
        logRepository.save(MachineLogEntry.of(machine, "reserved"));

        publish(EventType.MACHINE_RESERVED, new MachineReservedPayload(
                machine.getId(),
                bookingId,
                userId,
                machine.getBusinessStatus(),
                machine.getTechnicalStatus()
        ));
        publish(EventType.MACHINE_STATUS_CHANGED, new MachineStatusChangedPayload(
                machine.getId(),
                machine.getBusinessStatus(),
                machine.getTechnicalStatus(),
                "reserved"
        ));
        return MachineResponse.from(machine);
    }

    @Transactional
    @CacheEvict(value = {"devices", "freeDevices", "deviceById"}, allEntries = true)
    public MachineResponse freeMachine(UUID machineId, UUID bookingId, String reason) {
        Machine machine = repository.findLockedById(machineId).orElseThrow(() -> new NotFoundException("Машина не найдена"));
        if (bookingId != null && machine.getActiveBookingId() != null && !bookingId.equals(machine.getActiveBookingId())) {
            return MachineResponse.from(machine);
        }

        machine.free();
        repository.save(machine);
        logRepository.save(MachineLogEntry.of(machine, reason));

        publish(EventType.MACHINE_STATUS_CHANGED, new MachineStatusChangedPayload(
                machine.getId(),
                machine.getBusinessStatus(),
                machine.getTechnicalStatus(),
                reason
        ));
        return MachineResponse.from(machine);
    }

    @Transactional
    @CacheEvict(value = {"devices", "freeDevices", "deviceById"}, allEntries = true)
    public void onReservationExpired(ReservationExpiredPayload payload) {
        freeMachine(payload.machineId(), payload.bookingId(), payload.reason());
    }

    @Transactional
    @CacheEvict(value = {"devices", "freeDevices", "deviceById"}, allEntries = true)
    public void onWashStarted(WashStartedPayload payload) {
        Machine machine = repository.findLockedById(payload.machineId()).orElseThrow(() -> new NotFoundException("Машина не найдена"));
        machine.markBusy(payload.bookingId(), payload.userId());
        repository.save(machine);
        logRepository.save(MachineLogEntry.of(machine, "wash-started"));

        publish(EventType.MACHINE_STATUS_CHANGED, new MachineStatusChangedPayload(
                machine.getId(),
                machine.getBusinessStatus(),
                machine.getTechnicalStatus(),
                "wash-started"
        ));
    }

    @Transactional
    @CacheEvict(value = {"devices", "freeDevices", "deviceById"}, allEntries = true)
    public void onWashFinished(WashFinishedPayload payload) {
        freeMachine(payload.machineId(), payload.bookingId(), "wash-finished");
    }

    @Transactional
    @CacheEvict(value = {"devices", "freeDevices", "deviceById"}, allEntries = true)
    public void seedMachineIfMissing(String name, String location) {
        boolean exists = repository.findAll().stream().anyMatch(machine -> machine.getName().equalsIgnoreCase(name));
        if (!exists) {
            repository.save(Machine.create(name, location));
        }
    }

    private Machine findMachine(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Машина не найдена"));
    }

    private void publish(String topic, Object payload) {
        try {
            DomainEvent event = DomainEvent.of(topic, CorrelationIdHolder.get(), payload, objectMapper);
            kafkaTemplate.send(topic, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to publish event", exception);
        }
    }

    public record MachineResponse(
            UUID id,
            String name,
            String location,
            MachineBusinessStatus businessStatus,
            MachineTechnicalStatus technicalStatus,
            UUID activeBookingId,
            UUID activeUserId
    ) {
        public static MachineResponse from(Machine machine) {
            boolean hasActiveAssociation = machine.getBusinessStatus() != MachineBusinessStatus.FREE
                    && machine.getActiveBookingId() != null;
            UUID activeBookingId = hasActiveAssociation ? machine.getActiveBookingId() : null;
            UUID activeUserId = hasActiveAssociation ? machine.getActiveUserId() : null;
            return new MachineResponse(
                    machine.getId(),
                    machine.getName(),
                    machine.getLocation(),
                    machine.getBusinessStatus(),
                    machine.getTechnicalStatus(),
                    activeBookingId,
                    activeUserId
            );
        }
    }
}
