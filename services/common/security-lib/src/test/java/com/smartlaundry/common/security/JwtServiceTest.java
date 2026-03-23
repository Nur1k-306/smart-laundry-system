package com.smartlaundry.common.security;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void shouldGenerateAndParseToken() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("smart-laundry-super-secret-key-please-change");
        JwtService jwtService = new JwtService(properties);
        AuthenticatedUser source = new AuthenticatedUser(UUID.randomUUID(), "user@laundry.local", Role.USER);

        String token = jwtService.generateToken(source);
        AuthenticatedUser parsed = jwtService.parseToken(token);

        assertThat(parsed.userId()).isEqualTo(source.userId());
        assertThat(parsed.email()).isEqualTo(source.email());
        assertThat(parsed.role()).isEqualTo(source.role());
    }
}
