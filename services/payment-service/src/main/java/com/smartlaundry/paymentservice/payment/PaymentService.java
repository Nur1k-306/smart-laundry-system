package com.smartlaundry.paymentservice.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlaundry.common.events.DomainEvent;
import com.smartlaundry.common.events.EventPayloads.PaymentConfirmedPayload;
import com.smartlaundry.common.events.EventPayloads.PaymentCreatedPayload;
import com.smartlaundry.common.events.EventPayloads.PaymentRejectedPayload;
import com.smartlaundry.common.events.EventPayloads.ReservationExpiredPayload;
import com.smartlaundry.common.events.EventType;
import com.smartlaundry.common.security.Role;
import com.smartlaundry.common.web.BadRequestException;
import com.smartlaundry.common.web.ConflictException;
import com.smartlaundry.common.web.CorrelationIdHolder;
import com.smartlaundry.common.web.NotFoundException;
import com.smartlaundry.common.web.RoleGuard;
import com.smartlaundry.common.web.UserContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final BookingClient bookingClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentService(
            PaymentRepository repository,
            BookingClient bookingClient,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.bookingClient = bookingClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PaymentResponse create(UUID bookingId, BigDecimal amount) {
        UserContext user = RoleGuard.requireAuthenticated();
        if (amount == null || amount.signum() <= 0) {
            throw new BadRequestException("Сумма должна быть больше нуля");
        }

        BookingClient.BookingSummary booking = bookingClient.getBooking(bookingId);
        if (!booking.userId().equals(user.userId())) {
            throw new BadRequestException("Бронь не принадлежит текущему пользователю");
        }
        if (!"RESERVED".equals(booking.status())) {
            throw new BadRequestException("Оплату можно создать только для активной брони");
        }

        repository.findByBookingIdAndStatus(bookingId, PaymentStatus.PENDING).ifPresent(payment -> {
            throw new ConflictException("Для этой брони уже есть заявка на оплату");
        });
        repository.findByBookingIdAndStatus(bookingId, PaymentStatus.CONFIRMED).ifPresent(payment -> {
            throw new ConflictException("Для этой брони уже есть подтвержденная оплата");
        });

        Payment payment = repository.save(Payment.create(bookingId, user.userId(), booking.machineId(), amount));
        publish(EventType.PAYMENT_CREATED, new PaymentCreatedPayload(
                payment.getId(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getMachineId(),
                payment.getAmount(),
                payment.getStatus().name()
        ));
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse confirm(UUID paymentId) {
        RoleGuard.requireRole(Role.ADMIN);
        Payment payment = repository.findById(paymentId).orElseThrow(() -> new NotFoundException("Оплата не найдена"));
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Подтвердить можно только оплату в ожидании");
        }

        BookingClient.BookingSummary booking = bookingClient.getBooking(payment.getBookingId());
        if (!"RESERVED".equals(booking.status())) {
            throw new BadRequestException("Бронь уже недоступна для оплаты");
        }

        payment.confirm();
        repository.save(payment);
        publish(EventType.PAYMENT_CONFIRMED, new PaymentConfirmedPayload(
                payment.getId(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getMachineId(),
                payment.getAmount()
        ));
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse reject(UUID paymentId, String reason) {
        RoleGuard.requireRole(Role.ADMIN);
        Payment payment = repository.findById(paymentId).orElseThrow(() -> new NotFoundException("Оплата не найдена"));
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Отклонить можно только оплату в ожидании");
        }

        String resolvedReason = reason == null || reason.isBlank() ? "Отклонено администратором" : reason;
        payment.reject(resolvedReason);
        repository.save(payment);
        publish(EventType.PAYMENT_REJECTED, new PaymentRejectedPayload(
                payment.getId(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getMachineId(),
                resolvedReason
        ));
        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> pendingPayments() {
        RoleGuard.requireRole(Role.ADMIN);
        return repository.findAllByStatusOrderByCreatedAtAsc(PaymentStatus.PENDING).stream().map(PaymentResponse::from).toList();
    }

    @Transactional
    public void onReservationExpired(ReservationExpiredPayload payload) {
        repository.findAllByBookingIdAndStatus(payload.bookingId(), PaymentStatus.PENDING)
                .forEach(payment -> {
                    payment.reject("Бронь истекла");
                    repository.save(payment);
                });
    }

    private void publish(String topic, Object payload) {
        try {
            DomainEvent event = DomainEvent.of(topic, CorrelationIdHolder.get(), payload, objectMapper);
            kafkaTemplate.send(topic, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to publish payment event", exception);
        }
    }

    public record PaymentResponse(
            UUID id,
            UUID bookingId,
            UUID userId,
            UUID machineId,
            BigDecimal amount,
            PaymentStatus status,
            String rejectionReason
    ) {
        public static PaymentResponse from(Payment payment) {
            return new PaymentResponse(
                    payment.getId(),
                    payment.getBookingId(),
                    payment.getUserId(),
                    payment.getMachineId(),
                    payment.getAmount(),
                    payment.getStatus(),
                    payment.getRejectionReason()
            );
        }
    }
}
