package com.smartlaundry.paymentservice.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByBookingIdAndStatus(UUID bookingId, PaymentStatus status);

    List<Payment> findAllByStatusOrderByCreatedAtAsc(PaymentStatus status);

    List<Payment> findAllByBookingIdAndStatus(UUID bookingId, PaymentStatus status);
}
