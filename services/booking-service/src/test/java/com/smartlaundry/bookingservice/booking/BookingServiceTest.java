package com.smartlaundry.bookingservice.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingServiceTest {

    private BookingRepository repository;
    private DeviceClient deviceClient;
    private KafkaTemplate<String, String> kafkaTemplate;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        repository = mock(BookingRepository.class);
        deviceClient = mock(DeviceClient.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        bookingService = new BookingService(repository, deviceClient, kafkaTemplate, new ObjectMapper().findAndRegisterModules(), 90);
    }

    @Test
    void shouldExpireReservedBookingsAndPublishEvent() {
        Booking booking = Booking.create(UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now().minusMinutes(1));
        when(repository.findAllByStatusAndExpiresAtLessThanEqual(eq(BookingStatus.RESERVED), any())).thenReturn(List.of(booking));
        when(repository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bookingService.expireUnpaidBookings();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.EXPIRED);
        verify(repository).save(booking);
        verify(kafkaTemplate).send(eq("reservation_expired"), any(String.class));
    }
}
