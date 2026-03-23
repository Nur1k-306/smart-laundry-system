package com.smartlaundry.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.services")
public class ServiceRoutesProperties {

    private String userService;
    private String deviceService;
    private String bookingService;
    private String paymentService;
    private String washService;
    private String notificationService;
    private String analyticsService;

    public String getUserService() {
        return userService;
    }

    public void setUserService(String userService) {
        this.userService = userService;
    }

    public String getDeviceService() {
        return deviceService;
    }

    public void setDeviceService(String deviceService) {
        this.deviceService = deviceService;
    }

    public String getBookingService() {
        return bookingService;
    }

    public void setBookingService(String bookingService) {
        this.bookingService = bookingService;
    }

    public String getPaymentService() {
        return paymentService;
    }

    public void setPaymentService(String paymentService) {
        this.paymentService = paymentService;
    }

    public String getWashService() {
        return washService;
    }

    public void setWashService(String washService) {
        this.washService = washService;
    }

    public String getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(String notificationService) {
        this.notificationService = notificationService;
    }

    public String getAnalyticsService() {
        return analyticsService;
    }

    public void setAnalyticsService(String analyticsService) {
        this.analyticsService = analyticsService;
    }
}
