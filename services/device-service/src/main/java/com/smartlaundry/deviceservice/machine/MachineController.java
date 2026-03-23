package com.smartlaundry.deviceservice.machine;

import com.smartlaundry.common.events.MachineTechnicalStatus;
import com.smartlaundry.common.security.Role;
import com.smartlaundry.common.web.RoleGuard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @GetMapping("/devices")
    public List<MachineService.MachineResponse> getDevices() {
        RoleGuard.requireAuthenticated();
        return machineService.getAll();
    }

    @GetMapping("/devices/free")
    public List<MachineService.MachineResponse> getFreeDevices() {
        RoleGuard.requireAuthenticated();
        return machineService.getFree();
    }

    @GetMapping("/devices/{id}")
    public MachineService.MachineResponse getDevice(@PathVariable UUID id) {
        RoleGuard.requireAuthenticated();
        return machineService.getById(id);
    }

    @PatchMapping("/devices/{id}/technical-status")
    public MachineService.MachineResponse updateTechnicalStatus(@PathVariable UUID id, @Valid @RequestBody UpdateTechnicalStatusRequest request) {
        RoleGuard.requireRole(Role.ADMIN);
        return machineService.updateTechnicalStatus(id, request.technicalStatus());
    }

    @GetMapping("/admin/devices/faults")
    public List<MachineService.MachineResponse> getFaults() {
        RoleGuard.requireRole(Role.ADMIN);
        return machineService.getFaults();
    }

    @PostMapping("/internal/devices/{id}/reserve")
    public MachineService.MachineResponse reserveInternal(@PathVariable UUID id, @Valid @RequestBody ReserveMachineRequest request) {
        return machineService.reserveMachine(id, request.bookingId(), request.userId());
    }

    @PostMapping("/internal/devices/{id}/free")
    public MachineService.MachineResponse freeInternal(@PathVariable UUID id, @Valid @RequestBody FreeMachineRequest request) {
        return machineService.freeMachine(id, request.bookingId(), request.reason());
    }

    public record UpdateTechnicalStatusRequest(@NotNull MachineTechnicalStatus technicalStatus) {
    }

    public record ReserveMachineRequest(@NotNull UUID bookingId, @NotNull UUID userId) {
    }

    public record FreeMachineRequest(UUID bookingId, @NotBlank String reason) {
    }
}
