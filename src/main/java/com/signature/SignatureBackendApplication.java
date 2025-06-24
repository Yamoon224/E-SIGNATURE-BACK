package com.signature;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.signature")
public class SignatureBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SignatureBackendApplication.class, args);
    }
}
