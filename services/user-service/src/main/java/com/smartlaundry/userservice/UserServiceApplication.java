package com.smartlaundry.userservice;

import com.smartlaundry.common.security.SecurityLibraryConfiguration;
import com.smartlaundry.common.web.CommonWebConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({CommonWebConfiguration.class, SecurityLibraryConfiguration.class})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
