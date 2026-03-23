package com.smartlaundry.notificationservice.notification;

import com.smartlaundry.common.security.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
public class UserClient {

    private final RestClient restClient;

    public UserClient(RestClient.Builder builder, @Value("${app.user-service-url:http://localhost:8081}") String userServiceUrl) {
        this.restClient = builder.baseUrl(userServiceUrl).build();
    }

    public List<UserSummary> getUsersByRole(Role role) {
        UserSummary[] users = restClient.get()
                .uri("/internal/users/by-role/{role}", role.name())
                .retrieve()
                .body(UserSummary[].class);
        return users == null ? List.of() : List.of(users);
    }

    public record UserSummary(
            UUID id,
            String email,
            String fullName,
            Role role
    ) {
    }
}
