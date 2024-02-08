package com.toss.tosspaybackend;

import com.toss.tosspaybackend.config.security.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class TossPayBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TossPayBackendApplication.class, args);
    }

}
