package com.smartlaundry.common.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityLibraryConfiguration {

    @Bean
    public JwtService jwtService(JwtProperties properties) {
        return new JwtService(properties);
    }
}
