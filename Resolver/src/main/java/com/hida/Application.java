package com.hida;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Serves as an entry point for Spring Boot. This is where all the
 * configurations and beans are wired in its Application Context.
 *
 * @author lruffin
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
