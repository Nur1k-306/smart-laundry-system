package com.smartlaundry.common.web;

import java.time.OffsetDateTime;

public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId
) {
}
