package com.smartlaundry.common.web;

import com.smartlaundry.common.security.Role;

public final class RoleGuard {

    private RoleGuard() {
    }

    public static UserContext requireAuthenticated() {
        UserContext context = UserContextHolder.get();
        if (context == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return context;
    }

    public static UserContext requireRole(Role role) {
        UserContext context = requireAuthenticated();
        if (context.role() != role) {
            throw new AccessDeniedException("Required role: " + role);
        }
        return context;
    }
}
