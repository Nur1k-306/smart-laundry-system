package com.smartlaundry.common.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class HeaderPropagationInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(org.springframework.http.HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        HttpHeaders headers = request.getHeaders();
        String correlationId = CorrelationIdHolder.get();
        if (correlationId != null) {
            headers.set(CorrelationIdFilter.HEADER_NAME, correlationId);
        }

        UserContext userContext = UserContextHolder.get();
        if (userContext != null) {
            headers.set(UserContextFilter.USER_ID_HEADER, userContext.userId().toString());
            headers.set(UserContextFilter.USER_EMAIL_HEADER, userContext.email());
            headers.set(UserContextFilter.USER_ROLE_HEADER, userContext.role().name());
        }

        return execution.execute(request, body);
    }
}
