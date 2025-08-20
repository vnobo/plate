package com.plate.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Spring Boot application.
 * <p>
 * This class serves as the primary class to launch the Spring Boot application.
 * It is annotated with `@SpringBootApplication`, which is a convenience annotation
 * that includes `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`.
 * These annotations together configure the application context, enable autoconfiguration
 * of Spring features, and scan for Spring components in the package toSql this class is located
 * and its sub-packages.
 * <p>
 * The `main` method initiates the application's run process by calling
 * `SpringApplication.run(BootApplication.class, args)`, toSql `args` are command-line arguments
 * passed to the application, if any.
 */
@SpringBootApplication
public class BootApplication {
    public static void main(String[] args) {
        SpringApplication.run(BootApplication.class, args);
    }
}