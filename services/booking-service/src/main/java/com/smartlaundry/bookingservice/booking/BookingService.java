package com.smartlaundry.bookingservice.booking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlaundry.common.events.DomainEvent;
import com.smartlaundry.common.events.EventPayloads.BookingCreatedPayload;
import com.smartlaundry.common.events.EventPayloads.PaymentConfirmedPayload;
import com.smartlaundry.common.events.EventPayloads.PaymentRejectedPayload;
import com.smartlaundry.common.events.EventPayloads.ReservationExpiredPayload;
import com.smartlaundry.common.events.EventType;
import com.smartlaundry.common.web.BadRequestException;
import com.smartlaundry.common.web.CorrelationIdHolder;
import com.smartlaundry.common.web.NotFoundException;
import com.smartlaundry.common.web.RoleGuard;
import com.smartlaundry.common.web.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository repository;
    private final DeviceClient deviceClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final long expirationSeconds;

    public BookingService(
            BookingRepository repository,
            DeviceClient deviceClient,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.booking.expiration-seconds:90}") long expirationSeconds
    ) {
        this.repository = repository;
        this.deviceClient = deviceClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("app.booking.expiration-seconds must be positive");
        }
        this.expirationSeconds = expirationSeconds;
    }

    @Transactional
    public BookingResponse createBooking(UUID machineId) {
        UserContext user = RoleGuard.requireAuthenticated();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(expirationSeconds);
        Booking booking = Booking.create(user.userId(), machineId, expiresAt);

        deviceClient.reserveMachine(machineId, booking.getId(), user.userId());
        try {
            repository.save(booking);
            publish(EventType.BOOKING_CREATED, new BookingCreatedPayload(
                    booking.getId(),
                    booking.getUserId(),
                    booking.getMachineId(),
                    booking.getExpiresAt()
            ));
            return BookingResponse.from(booking);
        } catch (RuntimeException exception) {
            deviceClient.freeMachine(machineId, booking.getId(), "booking-create-rollback");
            throw exception;
        }
    }

    @Transactional
    public void cancelBooking(UUID bookingId) {
        UserContext user = RoleGuard.requireAuthenticated();
        Booking booking = repository.findByIdAndUserId(bookingId, user.userId())
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        if (booking.getStatus() != BookingStatus.RESERVED) {
            throw new BadRequestException("Можно отменить только активную бронь");
        }

        booking.markCancelled();
        repository.save(booking);
        deviceClient.freeMachine(booking.getMachineId(), booking.getId(), "cancelled-by-user");
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {
        UserContext user = RoleGuard.requireAuthenticated();
        return repository.findAllByUserIdOrderByCreatedAtDesc(user.userId()).stream().map(BookingResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse findForInternal(UUID bookingId) {
        return repository.findById(bookingId)
                .map(BookingResponse::from)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
    }

    @Transactional
    public void onPaymentConfirmed(PaymentConfirmedPayload payload) {
        repository.findById(payload.bookingId()).ifPresent(booking -> {
            booking.markPaid();
            repository.save(booking);
        });
    }

    @Transactional
    public void onPaymentRejected(PaymentRejectedPayload payload) {
        repository.findById(payload.bookingId()).ifPresent(booking -> {
            if (booking.getStatus() == BookingStatus.RESERVED) {
                booking.markPaymentRejected();
                repository.save(booking);
                deviceClient.freeMachine(booking.getMachineId(), booking.getId(), "payment-rejected");
            }
        });
    }

    @Scheduled(fixedDelayString = "${app.booking.expiration-check-ms:5000}")
    @Transactional
    public void expireUnpaidBookings() {
        List<Booking> bookings = repository.findAllByStatusAndExpiresAtLessThanEqual(BookingStatus.RESERVED, OffsetDateTime.now());
        bookings.forEach(booking -> {
            booking.markExpired();
            repository.save(booking);
            publish(EventType.RESERVATION_EXPIRED, new ReservationExpiredPayload(
                    booking.getId(),
                    booking.getUserId(),
                    booking.getMachineId(),
                    "reservation-expired"
            ));
        });
    }

    private void publish(String topic, Object payload) {
        try {
            DomainEvent event = DomainEvent.of(topic, CorrelationIdHolder.get(), payload, objectMapper);
            kafkaTemplate.send(topic, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to publish booking event", exception);
        }
    }

    public record BookingResponse(
            UUID id,
            UUID userId,
            UUID machineId,
            BookingStatus status,
            OffsetDateTime expiresAt,
            OffsetDateTime createdAt
    ) {
        public static BookingResponse from(Booking booking) {
            return new BookingResponse(
                    booking.getId(),
                    booking.getUserId(),
                    booking.getMachineId(),
                    booking.getStatus(),
                    booking.getExpiresAt(),
                    booking.getCreatedAt()
            );
        }
    }
}
