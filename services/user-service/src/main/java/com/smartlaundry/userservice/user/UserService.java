package com.smartlaundry.userservice.user;

import com.smartlaundry.common.security.AuthenticatedUser;
import com.smartlaundry.common.security.JwtService;
import com.smartlaundry.common.security.Role;
import com.smartlaundry.common.web.ConflictException;
import com.smartlaundry.common.web.NotFoundException;
import com.smartlaundry.common.web.UnauthorizedException;
import com.smartlaundry.userservice.api.AuthController.AuthResponse;
import com.smartlaundry.userservice.api.AuthController.LoginRequest;
import com.smartlaundry.userservice.api.AuthController.RegisterRequest;
import com.smartlaundry.userservice.api.AuthController.UserSummary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserAccountRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        repository.findByEmailIgnoreCase(request.email()).ifPresent(user -> {
            throw new ConflictException("Пользователь с таким email уже существует");
        });

        UserAccount user = repository.save(UserAccount.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.fullName(),
                Role.USER
        ));

        return new AuthResponse(tokenFor(user), UserSummary.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserAccount user = repository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UnauthorizedException("Неверный email или пароль"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Неверный email или пароль");
        }

        return new AuthResponse(tokenFor(user), UserSummary.from(user));
    }

    @Transactional(readOnly = true)
    public List<UserAccount> findByRole(Role role) {
        return repository.findByRole(role);
    }

    @Transactional(readOnly = true)
    public UserAccount findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private String tokenFor(UserAccount user) {
        return jwtService.generateToken(new AuthenticatedUser(user.getId(), user.getEmail(), user.getRole()));
    }
}
