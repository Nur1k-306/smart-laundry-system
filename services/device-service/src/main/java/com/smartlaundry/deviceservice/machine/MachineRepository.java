package com.smartlaundry.deviceservice.machine;

import com.smartlaundry.common.events.MachineBusinessStatus;
import com.smartlaundry.common.events.MachineTechnicalStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MachineRepository extends JpaRepository<Machine, UUID> {

    List<Machine> findAllByBusinessStatus(MachineBusinessStatus businessStatus);

    List<Machine> findAllByTechnicalStatus(MachineTechnicalStatus technicalStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Machine m where m.id = :id")
    Optional<Machine> findLockedById(UUID id);
}
