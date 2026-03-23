package com.smartlaundry.common.web;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackageClasses = GlobalExceptionHandler.class)
public class CommonWebConfiguration {

    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }

    @Bean
    public UserContextFilter userContextFilter() {
        return new UserContextFilter();
    }

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return builder -> builder.requestInterceptor(new HeaderPropagationInterceptor());
    }
}
