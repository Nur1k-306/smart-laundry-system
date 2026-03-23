package com.smartlaundry.washservice;

import com.smartlaundry.common.web.CommonWebConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Import(CommonWebConfiguration.class)
public class WashSessionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WashSessionServiceApplication.class, args);
    }
}
