package com.smartlaundry.deviceservice.machine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlaundry.common.events.DomainEvent;
import com.smartlaundry.common.events.EventPayloads.ReservationExpiredPayload;
import com.smartlaundry.common.events.EventPayloads.WashFinishedPayload;
import com.smartlaundry.common.events.EventPayloads.WashStartedPayload;
import com.smartlaundry.common.events.EventType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MachineEventConsumer {

    private final MachineService machineService;
    private final ObjectMapper objectMapper;

    public MachineEventConsumer(MachineService machineService, ObjectMapper objectMapper) {
        this.machineService = machineService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = EventType.RESERVATION_EXPIRED, groupId = "device-service")
    public void onReservationExpired(String payload) throws JsonProcessingException {
        DomainEvent event = objectMapper.readValue(payload, DomainEvent.class);
        machineService.onReservationExpired(event.payloadAs(objectMapper, ReservationExpiredPayload.class));
    }

    @KafkaListener(topics = EventType.WASH_STARTED, groupId = "device-service")
    public void onWashStarted(String payload) throws JsonProcessingException {
        DomainEvent event = objectMapper.readValue(payload, DomainEvent.class);
        machineService.onWashStarted(event.payloadAs(objectMapper, WashStartedPayload.class));
    }

    @KafkaListener(topics = EventType.WASH_FINISHED, groupId = "device-service")
    public void onWashFinished(String payload) throws JsonProcessingException {
        DomainEvent event = objectMapper.readValue(payload, DomainEvent.class);
        machineService.onWashFinished(event.payloadAs(objectMapper, WashFinishedPayload.class));
    }
}
