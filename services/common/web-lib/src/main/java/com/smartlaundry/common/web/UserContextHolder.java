package com.smartlaundry.common.web;

public final class UserContextHolder {

    private static final ThreadLocal<UserContext> USER_CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(UserContext context) {
        USER_CONTEXT.set(context);
    }

    public static UserContext get() {
        return USER_CONTEXT.get();
    }

    public static void clear() {
        USER_CONTEXT.remove();
    }
}
