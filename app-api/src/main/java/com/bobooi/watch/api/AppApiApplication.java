package com.bobooi.watch.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.bobooi.watch")
@EnableJpaRepositories(basePackages = "com.bobooi.watch.data.repository.concrete")
@EntityScan(basePackages="com.bobooi.watch.data.entity")
public class AppApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppApiApplication.class, args);
    }

}



