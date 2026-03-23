package com.smartlaundry.userservice.config;

import com.smartlaundry.common.security.Role;
import com.smartlaundry.userservice.user.UserAccount;
import com.smartlaundry.userservice.user.UserAccountRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
public class UserSeedData {

    @Bean
    public ApplicationRunner userSeeder(UserAccountRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (repository.findByEmailIgnoreCase("admin@laundry.local").isEmpty()) {
                repository.save(UserAccount.create(
                        "admin@laundry.local",
                        passwordEncoder.encode("admin12345"),
                        "System Admin",
                        Role.ADMIN
                ));
            }

            if (repository.findByEmailIgnoreCase("user@laundry.local").isEmpty()) {
                repository.save(UserAccount.create(
                        "user@laundry.local",
                        passwordEncoder.encode("user12345"),
                        "Regular User",
                        Role.USER
                ));
            }
        };
    }
}
