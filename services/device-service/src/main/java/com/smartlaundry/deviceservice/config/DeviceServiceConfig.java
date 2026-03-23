package com.smartlaundry.deviceservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class DeviceServiceConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .entryTtl(Duration.ofMinutes(2));
        return RedisCacheManager.builder(connectionFactory).cacheDefaults(configuration).build();
    }

    @Bean
    public ApplicationRunner clearDeviceCachesOnStartup(CacheManager cacheManager) {
        return args -> {
            clearCache(cacheManager, "devices");
            clearCache(cacheManager, "freeDevices");
            clearCache(cacheManager, "deviceById");
        };
    }

    private static void clearCache(CacheManager cacheManager, String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

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
