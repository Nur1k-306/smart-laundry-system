package com.smartlaundry.notificationservice.notification;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceSubscriptionRepository extends MongoRepository<DeviceSubscription, String> {

    Optional<DeviceSubscription> findByUserIdAndMachineIdAndActiveTrue(UUID userId, UUID machineId);

    List<DeviceSubscription> findAllByMachineIdAndActiveTrue(UUID machineId);
}
