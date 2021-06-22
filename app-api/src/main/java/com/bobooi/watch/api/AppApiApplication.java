package com.bobooi.watch.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.bobooi.watch")
public class AppApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppApiApplication.class, args);
    }

}
