package com.plate.boot.security.jwt;

import com.plate.boot.security.SecurityManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration for the stateless JWT authentication subsystem.
 * <p>
 * Registers the {@link ClientType}-aware token service, the WeChat mini-program client,
 * the JWT security context repository and the username/password authentication manager
 * shared by the {@code /auth/login} endpoint. The WebFlux security filter chain consumes
 * the repository bean so both JWT and session transports stay active side by side.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({JwtProperties.class, WechatMiniAppProperties.class})
public class JwtConfiguration {

    /**
     * Creates the reactive authentication manager backing username/password logins
     * (PC and native app channels), delegating user lookup to {@link SecurityManager}.
     *
     * @param securityManager the user details service holding users, tenants and authorities
     * @param passwordEncoder the encoder used to verify presented passwords
     * @return the configured {@link ReactiveAuthenticationManager}
     */
    @Bean
    public ReactiveAuthenticationManager passwordAuthenticationManager(SecurityManager securityManager,
                                                                       PasswordEncoder passwordEncoder) {
        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(securityManager);
        manager.setPasswordEncoder(passwordEncoder);
        manager.setUserDetailsPasswordService(securityManager);
        return manager;
    }
}
