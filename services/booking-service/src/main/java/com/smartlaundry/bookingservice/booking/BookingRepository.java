package com.smartlaundry.bookingservice.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByIdAndUserId(UUID id, UUID userId);

    List<Booking> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Booking> findAllByStatusAndExpiresAtLessThanEqual(BookingStatus status, OffsetDateTime threshold);
}
