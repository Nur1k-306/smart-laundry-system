package com.smartlaundry.common.events;

public final class EventType {

    public static final String BOOKING_CREATED = "booking_created";
    public static final String MACHINE_RESERVED = "machine_reserved";
    public static final String PAYMENT_CREATED = "payment_created";
    public static final String PAYMENT_CONFIRMED = "payment_confirmed";
    public static final String PAYMENT_REJECTED = "payment_rejected";
    public static final String RESERVATION_EXPIRED = "reservation_expired";
    public static final String WASH_STARTED = "wash_started";
    public static final String WASH_FINISHED = "wash_finished";
    public static final String MACHINE_STATUS_CHANGED = "machine_status_changed";
    public static final String MACHINE_FAULT_DETECTED = "machine_fault_detected";
    public static final String NOTIFICATION_REQUESTED = "notification_requested";

    private EventType() {
    }
}
