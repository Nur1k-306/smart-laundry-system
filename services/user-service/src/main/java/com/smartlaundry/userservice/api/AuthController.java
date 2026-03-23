package com.smartlaundry.userservice.api;

import com.smartlaundry.common.security.Role;
import com.smartlaundry.userservice.user.UserAccount;
import com.smartlaundry.userservice.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/auth/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/internal/users/by-role/{role}")
    public List<UserSummary> findByRole(@PathVariable Role role) {
        return userService.findByRole(role).stream().map(UserSummary::from).toList();
    }

    @GetMapping("/internal/users/{id}")
    public UserSummary findById(@PathVariable UUID id) {
        return UserSummary.from(userService.findById(id));
    }

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 128) String password,
            @NotBlank @Size(max = 128) String fullName
    ) {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }

    public record AuthResponse(
            String token,
            UserSummary user
    ) {
    }

    public record UserSummary(
            UUID id,
            String email,
            String fullName,
            Role role
    ) {
        public static UserSummary from(UserAccount user) {
            return new UserSummary(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
        }
    }
}
