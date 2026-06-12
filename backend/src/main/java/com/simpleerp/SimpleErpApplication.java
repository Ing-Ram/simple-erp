package com.simpleerp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Entry point for the SimpleERP modular-monolith backend. */
@SpringBootApplication
public class SimpleErpApplication {

    /** Boots the Spring application context. */
    public static void main(String[] args) {
        SpringApplication.run(SimpleErpApplication.class, args);
    }
}
