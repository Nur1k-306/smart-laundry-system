package com.smartlaundry.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RedisRateLimitFilter implements WebFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final int requestsPerMinute;

    public RedisRateLimitFilter(
            ReactiveStringRedisTemplate redisTemplate,
            @Value("${app.rate-limit.requests-per-minute:120}") int requestsPerMinute
    ) {
        this.redisTemplate = redisTemplate;
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }

        String client = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (client == null || client.isBlank()) {
            client = exchange.getRequest().getRemoteAddress() == null
                    ? "anonymous"
                    : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }

        String bucket = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String key = "gateway:rate-limit:" + client + ":" + bucket;

        return redisTemplate.opsForValue().increment(key)
                .flatMap(value -> {
                    if (value == 1) {
                        return redisTemplate.expire(key, Duration.ofMinutes(1)).thenReturn(value);
                    }
                    return Mono.just(value);
                })
                .flatMap(value -> {
                    if (value > requestsPerMinute) {
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        byte[] payload = "{\"message\":\"Rate limit exceeded\"}".getBytes(StandardCharsets.UTF_8);
                        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(payload)));
                    }
                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
