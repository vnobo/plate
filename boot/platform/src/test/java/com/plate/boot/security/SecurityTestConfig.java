package com.plate.boot.security;

import com.plate.boot.config.SecurityConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpBasicServerAuthenticationEntryPoint;

/**
 * Test configuration for security-related tests.
 * Provides minimal security configuration for testing SecurityController.
 */
@TestConfiguration
@EnableWebFluxSecurity
@Import(SecurityConfiguration.class)
public class SecurityTestConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Disable CSRF for testing
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/oauth2/**").permitAll()
                        .anyExchange().authenticated()
                ).httpBasic(httpBasicSpec -> httpBasicSpec
                        .authenticationEntryPoint(new HttpBasicServerAuthenticationEntryPoint()))
                .build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails testUser = User.withUsername("testuser")
                .password("{noop}password") // No-op for testing
                .roles("USER")
                .build();

        UserDetails adminUser = User.withUsername("admin")
                .password("{noop}adminpass")
                .roles("ADMIN")
                .build();

        return new MapReactiveUserDetailsService(testUser, adminUser);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}