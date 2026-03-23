package com.smartlaundry.paymentservice.payment;

import com.smartlaundry.common.web.BadRequestException;
import com.smartlaundry.common.web.ConflictException;
import com.smartlaundry.common.web.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class BookingClient {

    private final RestClient restClient;

    public BookingClient(RestClient.Builder builder, @Value("${app.booking-service-url:http://localhost:8083}") String bookingServiceUrl) {
        this.restClient = builder.baseUrl(bookingServiceUrl).build();
    }

    public BookingSummary getBooking(UUID bookingId) {
        try {
            return restClient.get()
                    .uri("/internal/bookings/{id}", bookingId)
                    .retrieve()
                    .body(BookingSummary.class);
        } catch (RestClientResponseException exception) {
            HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
            if (status == HttpStatus.NOT_FOUND) {
                throw new NotFoundException("Бронь не найдена");
            }
            if (status == HttpStatus.BAD_REQUEST) {
                throw new BadRequestException(exception.getResponseBodyAsString());
            }
            if (status == HttpStatus.CONFLICT) {
                throw new ConflictException(exception.getResponseBodyAsString());
            }
            throw exception;
        }
    }

    public record BookingSummary(
            UUID id,
            UUID userId,
            UUID machineId,
            String status,
            OffsetDateTime expiresAt,
            OffsetDateTime createdAt
    ) {
    }
}
