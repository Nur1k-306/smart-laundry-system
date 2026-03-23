package com.smartlaundry.gateway.security;

import com.smartlaundry.common.security.AuthenticatedUser;
import com.smartlaundry.common.security.JwtService;
import com.smartlaundry.common.security.Role;
import io.jsonwebtoken.JwtException;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class JwtAuthenticationFilter implements WebFilter, Ordered {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/register",
            "/auth/login",
            "/actuator",
            "/actuator/",
            "/actuator/health",
            "/actuator/prometheus"
    );
    private static final Pattern DEVICE_ADMIN_PATTERN = Pattern.compile("^/devices/[^/]+/technical-status$");
    private static final Pattern PAYMENT_ADMIN_PATTERN = Pattern.compile("^/payments/[^/]+/(confirm|reject)$");

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return error(exchange, HttpStatus.UNAUTHORIZED, "Missing bearer token");
        }

        try {
            AuthenticatedUser user = jwtService.parseToken(authorization.substring(7));
            if (requiresAdmin(exchange.getRequest().getMethod().name(), path) && user.role() != Role.ADMIN) {
                return error(exchange, HttpStatus.FORBIDDEN, "Admin role required");
            }

            ServerWebExchange mutated = exchange.mutate()
                    .request(builder -> builder.headers(headers -> {
                        headers.remove("X-User-Id");
                        headers.remove("X-User-Email");
                        headers.remove("X-User-Role");
                        headers.set("X-User-Id", user.userId().toString());
                        headers.set("X-User-Email", user.email());
                        headers.set("X-User-Role", user.role().name());
                    }))
                    .build();
            return chain.filter(mutated);
        } catch (JwtException | IllegalArgumentException exception) {
            return error(exchange, HttpStatus.UNAUTHORIZED, "Invalid bearer token");
        }
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean requiresAdmin(String method, String path) {
        return path.startsWith("/admin/")
                || ("PATCH".equalsIgnoreCase(method) && DEVICE_ADMIN_PATTERN.matcher(path).matches())
                || ("POST".equalsIgnoreCase(method) && PAYMENT_ADMIN_PATTERN.matcher(path).matches());
    }

    private Mono<Void> error(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] payload = ("{\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(payload)));
    }

    @Override
    public int getOrder() {
        return -150;
    }
}
