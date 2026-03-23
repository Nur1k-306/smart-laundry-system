package com.smartlaundry.analyticsservice.analytics;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventAuditRepository extends MongoRepository<EventAuditDocument, String> {
}
