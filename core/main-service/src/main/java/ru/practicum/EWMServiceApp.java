package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EWMServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EWMServiceApp.class, args);
    }
}
