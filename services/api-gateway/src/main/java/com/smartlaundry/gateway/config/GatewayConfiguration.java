package com.smartlaundry.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ServiceRoutesProperties.class)
public class GatewayConfiguration {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder, ServiceRoutesProperties properties) {
        return builder.routes()
                .route("user-service", route -> route.path("/auth/**").uri(properties.getUserService()))
                .route("device-service", route -> route.path("/devices/**", "/admin/devices/**").uri(properties.getDeviceService()))
                .route("booking-service", route -> route.path("/bookings/**").uri(properties.getBookingService()))
                .route("payment-service", route -> route.path("/payments/**", "/admin/payments/**").uri(properties.getPaymentService()))
                .route("wash-service", route -> route.path("/wash-sessions/**").uri(properties.getWashService()))
                .route("notification-service", route -> route.path("/notifications/**", "/subscriptions/**").uri(properties.getNotificationService()))
                .route("analytics-service", route -> route.path("/analytics/**").uri(properties.getAnalyticsService()))
                .build();
    }
}
