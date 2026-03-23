package com.smartlaundry.deviceservice.machine;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MachineServiceTest {

    @Test
    void shouldHideActiveMarkersForFreeMachineResponses() throws Exception {
        Machine machine = Machine.create("Washer 1", "Floor 1");
        setField(machine, "activeBookingId", UUID.randomUUID());
        setField(machine, "activeUserId", UUID.randomUUID());

        MachineService.MachineResponse response = MachineService.MachineResponse.from(machine);

        assertThat(response.businessStatus()).isEqualTo(com.smartlaundry.common.events.MachineBusinessStatus.FREE);
        assertThat(response.activeBookingId()).isNull();
        assertThat(response.activeUserId()).isNull();
    }

    @Test
    void shouldKeepActiveMarkersForNonFreeMachineResponses() {
        Machine machine = Machine.create("Washer 2", "Floor 1");
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        machine.reserve(bookingId, userId);

        MachineService.MachineResponse response = MachineService.MachineResponse.from(machine);

        assertThat(response.activeBookingId()).isEqualTo(bookingId);
        assertThat(response.activeUserId()).isEqualTo(userId);
    }

    @Test
    void shouldHideStaleUserMarkerWhenActiveBookingIsMissing() throws Exception {
        Machine machine = Machine.create("Washer 3", "Floor 2");
        setField(machine, "businessStatus", com.smartlaundry.common.events.MachineBusinessStatus.BUSY);
        setField(machine, "activeBookingId", null);
        setField(machine, "activeUserId", UUID.randomUUID());

        MachineService.MachineResponse response = MachineService.MachineResponse.from(machine);

        assertThat(response.activeBookingId()).isNull();
        assertThat(response.activeUserId()).isNull();
    }

    @Test
    void shouldAllowMachineReuseAfterItIsFreed() {
        Machine machine = Machine.create("Washer 4", "Floor 2");
        UUID firstBookingId = UUID.randomUUID();
        UUID secondBookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        machine.reserve(firstBookingId, userId);
        machine.markBusy(firstBookingId, userId);
        machine.free();
        machine.reserve(secondBookingId, userId);

        MachineService.MachineResponse response = MachineService.MachineResponse.from(machine);

        assertThat(response.businessStatus()).isEqualTo(com.smartlaundry.common.events.MachineBusinessStatus.RESERVED);
        assertThat(response.activeBookingId()).isEqualTo(secondBookingId);
        assertThat(response.activeUserId()).isEqualTo(userId);
    }

    private static void setField(Machine machine, String fieldName, Object value) throws Exception {
        Field field = Machine.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(machine, value);
    }
}
