package com.smartlaundry.analyticsservice.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlaundry.common.events.DomainEvent;
import com.smartlaundry.common.security.Role;
import com.smartlaundry.common.web.RoleGuard;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final EventAuditRepository repository;
    private final ObjectMapper objectMapper;
    private final MinioClient minioClient;
    private final String bucketName;

    public AnalyticsService(
            EventAuditRepository repository,
            ObjectMapper objectMapper,
            MinioClient minioClient,
            @Value("${app.minio.bucket:raw-events}") String bucketName
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    public void store(DomainEvent event) {
        try {
            ensureBucket();
            String eventJson = objectMapper.writeValueAsString(event);
            repository.save(EventAuditDocument.of(
                    event.eventId().toString(),
                    event.eventType(),
                    event.occurredAt(),
                    event.correlationId(),
                    eventJson
            ));
            uploadToMinio(event);
        } catch (Exception exception) {
            log.error("Failed to store analytics event {}", event.eventType(), exception);
        }
    }

    public AnalyticsSummary getSummary() {
        RoleGuard.requireRole(Role.ADMIN);
        List<EventAuditDocument> documents = repository.findAll();
        Map<String, Long> countsByType = documents.stream()
                .collect(Collectors.groupingBy(EventAuditDocument::getEventType, Collectors.counting()));
        OffsetDateTime lastEventAt = documents.stream()
                .map(EventAuditDocument::getOccurredAt)
                .max(OffsetDateTime::compareTo)
                .orElse(null);

        return new AnalyticsSummary(documents.size(), countsByType, lastEventAt);
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    private void uploadToMinio(DomainEvent event) throws Exception {
        String eventJson = objectMapper.writeValueAsString(event);
        byte[] bytes = eventJson.getBytes(StandardCharsets.UTF_8);
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(event.eventType() + "/" + event.eventId() + ".json")
                        .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                        .contentType("application/json")
                        .build()
        );
    }

    public record AnalyticsSummary(
            int totalEvents,
            Map<String, Long> countsByType,
            OffsetDateTime lastEventAt
    ) {
    }
}
