package com.smartlaundry.common.web;

import com.smartlaundry.common.security.Role;

import java.util.UUID;

public record UserContext(
        UUID userId,
        String email,
        Role role
) {
}
