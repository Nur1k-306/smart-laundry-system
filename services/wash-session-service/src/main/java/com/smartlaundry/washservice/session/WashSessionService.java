package com.smartlaundry.washservice.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlaundry.common.events.DomainEvent;
import com.smartlaundry.common.events.EventPayloads.PaymentConfirmedPayload;
import com.smartlaundry.common.events.EventPayloads.WashFinishedPayload;
import com.smartlaundry.common.events.EventPayloads.WashStartedPayload;
import com.smartlaundry.common.events.EventType;
import com.smartlaundry.common.web.BadRequestException;
import com.smartlaundry.common.web.CorrelationIdHolder;
import com.smartlaundry.common.web.RoleGuard;
import com.smartlaundry.common.web.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WashSessionService {

    private final WashSessionRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final long durationSeconds;

    public WashSessionService(
            WashSessionRepository repository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.wash.duration-seconds:180}") long durationSeconds
    ) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        if (durationSeconds <= 0) {
            throw new BadRequestException("app.wash.duration-seconds must be positive");
        }
        this.durationSeconds = durationSeconds;
    }

    public void onPaymentConfirmed(PaymentConfirmedPayload payload) {
        if (repository.findByPaymentId(payload.paymentId()).isPresent()) {
            return;
        }

        WashSession session = repository.save(WashSession.start(
                payload.bookingId(),
                payload.paymentId(),
                payload.userId(),
                payload.machineId(),
                durationSeconds
        ));
        publish(EventType.WASH_STARTED, new WashStartedPayload(
                session.getId(),
                session.getBookingId(),
                session.getPaymentId(),
                session.getUserId(),
                session.getMachineId(),
                session.getStartedAt(),
                session.getExpectedEndAt()
        ));
    }

    @Scheduled(fixedDelayString = "${app.wash.completion-check-ms:5000}")
    public void completeFinishedSessions() {
        java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
        List<WashSession> sessions = repository.findAllByStatus(WashSessionStatus.RUNNING).stream()
                .filter(session -> session.getExpectedEndAt() != null && !session.getExpectedEndAt().isAfter(now))
                .toList();
        sessions.forEach(session -> {
            session.finish();
            repository.save(session);
            publish(EventType.WASH_FINISHED, new WashFinishedPayload(
                    session.getId(),
                    session.getBookingId(),
                    session.getUserId(),
                    session.getMachineId(),
                    session.getFinishedAt()
            ));
        });
    }

    public List<WashSessionResponse> getMine() {
        UserContext user = RoleGuard.requireAuthenticated();
        return repository.findAllByUserIdOrderByStartedAtDesc(user.userId()).stream().map(WashSessionResponse::from).toList();
    }

    private void publish(String topic, Object payload) {
        try {
            DomainEvent event = DomainEvent.of(topic, CorrelationIdHolder.get(), payload, objectMapper);
            kafkaTemplate.send(topic, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to publish wash event", exception);
        }
    }

    public record WashSessionResponse(
            String id,
            java.util.UUID bookingId,
            java.util.UUID paymentId,
            java.util.UUID userId,
            java.util.UUID machineId,
            WashSessionStatus status,
            java.time.OffsetDateTime startedAt,
            java.time.OffsetDateTime expectedEndAt,
            java.time.OffsetDateTime finishedAt
    ) {
        public static WashSessionResponse from(WashSession session) {
            return new WashSessionResponse(
                    session.getId(),
                    session.getBookingId(),
                    session.getPaymentId(),
                    session.getUserId(),
                    session.getMachineId(),
                    session.getStatus(),
                    session.getStartedAt(),
                    session.getExpectedEndAt(),
                    session.getFinishedAt()
            );
        }
    }
}
