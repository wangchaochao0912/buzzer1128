package com.example.buzzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example.buzzer")
@EntityScan("com.example.buzzer.entity")
@EnableJpaRepositories("com.example.buzzer.repository")
public class BuzzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuzzerApplication.class, args);
    }

}