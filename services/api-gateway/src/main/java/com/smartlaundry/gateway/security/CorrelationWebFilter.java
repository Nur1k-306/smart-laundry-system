package com.smartlaundry.gateway.security;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationWebFilter implements WebFilter, Ordered {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        String finalCorrelationId = correlationId;
        ServerWebExchange mutated = exchange.mutate()
                .request(builder -> builder.headers(headers -> headers.set(CORRELATION_ID_HEADER, finalCorrelationId)))
                .build();
        mutated.getResponse().getHeaders().set(CORRELATION_ID_HEADER, finalCorrelationId);
        return chain.filter(mutated);
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
