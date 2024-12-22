package com.plate.boot.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, SecurityConfiguration.class})
@EnableTransactionManagement
public class InfrastructureConfiguration {
}
