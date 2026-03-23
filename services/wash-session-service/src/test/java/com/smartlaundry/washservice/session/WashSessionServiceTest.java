package com.smartlaundry.washservice.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlaundry.common.events.EventPayloads.PaymentConfirmedPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WashSessionServiceTest {

    private WashSessionRepository repository;
    private KafkaTemplate<String, String> kafkaTemplate;
    private WashSessionService washSessionService;

    @BeforeEach
    void setUp() {
        repository = mock(WashSessionRepository.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        washSessionService = new WashSessionService(
                repository,
                kafkaTemplate,
                new ObjectMapper().findAndRegisterModules(),
                180
        );
    }

    @Test
    void shouldCreateWashSessionUsingConfiguredDurationInSeconds() {
        UUID bookingId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID machineId = UUID.randomUUID();
        when(repository.findByPaymentId(paymentId)).thenReturn(Optional.empty());
        when(repository.save(any(WashSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        washSessionService.onPaymentConfirmed(new PaymentConfirmedPayload(
                paymentId,
                bookingId,
                userId,
                machineId,
                BigDecimal.valueOf(450)
        ));

        ArgumentCaptor<WashSession> sessionCaptor = ArgumentCaptor.forClass(WashSession.class);
        verify(repository).save(sessionCaptor.capture());
        WashSession savedSession = sessionCaptor.getValue();

        assertThat(Duration.between(savedSession.getStartedAt(), savedSession.getExpectedEndAt()).toSeconds())
                .isEqualTo(180);
        verify(kafkaTemplate).send(any(String.class), any(String.class));
    }
}
