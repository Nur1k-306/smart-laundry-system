package com.smartlaundry.deviceservice.machine;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MachineLogRepository extends MongoRepository<MachineLogEntry, String> {
}
