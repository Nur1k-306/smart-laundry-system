package com.smartlaundry.notificationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class NotificationServiceConfig {

    @Bean
    public NewTopic bookingCreatedTopic() {
        return new NewTopic("booking_created", 1, (short) 1);
    }

    @Bean
    public NewTopic machineReservedTopic() {
        return new NewTopic("machine_reserved", 1, (short) 1);
    }

    @Bean
    public NewTopic paymentCreatedTopic() {
        return new NewTopic("payment_created", 1, (short) 1);
    }

    @Bean
    public NewTopic paymentConfirmedTopic() {
        return new NewTopic("payment_confirmed", 1, (short) 1);
    }

    @Bean
    public NewTopic paymentRejectedTopic() {
        return new NewTopic("payment_rejected", 1, (short) 1);
    }

    @Bean
    public NewTopic reservationExpiredTopic() {
        return new NewTopic("reservation_expired", 1, (short) 1);
    }

    @Bean
    public NewTopic washStartedTopic() {
        return new NewTopic("wash_started", 1, (short) 1);
    }

    @Bean
    public NewTopic washFinishedTopic() {
        return new NewTopic("wash_finished", 1, (short) 1);
    }

    @Bean
    public NewTopic machineStatusChangedTopic() {
        return new NewTopic("machine_status_changed", 1, (short) 1);
    }

    @Bean
    public NewTopic machineFaultDetectedTopic() {
        return new NewTopic("machine_fault_detected", 1, (short) 1);
    }

    @Bean
    public NewTopic notificationRequestedTopic() {
        return new NewTopic("notification_requested", 1, (short) 1);
    }
}
