package com.smartlaundry.washservice.session;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WashSessionRepository extends MongoRepository<WashSession, String> {

    Optional<WashSession> findByPaymentId(UUID paymentId);

    List<WashSession> findAllByUserIdOrderByStartedAtDesc(UUID userId);

    List<WashSession> findAllByStatus(WashSessionStatus status);
}
