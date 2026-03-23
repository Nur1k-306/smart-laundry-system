package com.smartlaundry.bookingservice.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.smartlaundry.common.web.BadRequestException;
import com.smartlaundry.common.web.ConflictException;
import com.smartlaundry.common.web.NotFoundException;

import java.util.UUID;
import java.util.function.Supplier;

@Component
public class DeviceClient {

    private final RestClient restClient;

    public DeviceClient(RestClient.Builder builder, @Value("${app.device-service-url:http://localhost:8082}") String deviceServiceUrl) {
        this.restClient = builder.baseUrl(deviceServiceUrl).build();
    }

    public void reserveMachine(UUID machineId, UUID bookingId, UUID userId) {
        execute(() -> restClient.post()
                .uri("/internal/devices/{id}/reserve", machineId)
                .body(new ReserveMachineRequest(bookingId, userId))
                .retrieve()
                .toBodilessEntity());
    }

    public void freeMachine(UUID machineId, UUID bookingId, String reason) {
        execute(() -> restClient.post()
                .uri("/internal/devices/{id}/free", machineId)
                .body(new FreeMachineRequest(bookingId, reason))
                .retrieve()
                .toBodilessEntity());
    }

    private <T> T execute(Supplier<T> call) {
        try {
            return call.get();
        } catch (RestClientResponseException exception) {
            HttpStatus statusCode = HttpStatus.valueOf(exception.getStatusCode().value());
            String message = exception.getResponseBodyAsString();
            if (statusCode == HttpStatus.BAD_REQUEST) {
                throw new BadRequestException(message);
            }
            if (statusCode == HttpStatus.NOT_FOUND) {
                throw new NotFoundException("Машина не найдена");
            }
            if (statusCode == HttpStatus.CONFLICT) {
                throw new ConflictException(message);
            }
            throw new IllegalStateException("Device service error: " + statusCode.value() + " " + message, exception);
        }
    }

    private record ReserveMachineRequest(UUID bookingId, UUID userId) {
    }

    private record FreeMachineRequest(UUID bookingId, String reason) {
    }
}
