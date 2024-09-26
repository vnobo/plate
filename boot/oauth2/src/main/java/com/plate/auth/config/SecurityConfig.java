package com.plate.auth.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import static org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter.Directive.COOKIES;


/**
 * Provides the security configuration.
 *
 * @see <a href="https://docs.spring.io/spring-security/reference/servlet/configuration/java.html">Spring Security Reference</a>
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing
public class SecurityConfig {

    /**
     * Provides a {@link PasswordEncoder} to be used for password storage.
     * The {@link PasswordEncoder} is {@link org.springframework.security.crypto.factory.PasswordEncoderFactories#createDelegatingPasswordEncoder()}.
     *
     * @return a {@link PasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Publishes {@link HttpSession} events to the Spring
     * {@link org.springframework.context.ApplicationEventPublisher} so that
     * {@link org.springframework.security.web.session.HttpSessionEventPublisher}
     * can be used.
     *
     * @return an {@link HttpSessionEventPublisher} instance
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * Configures the {@link SecurityFilterChain} to require authentication for all requests,
     * except for static resources at common locations. The {@link SecurityFilterChain} uses
     * HTTP Basic authentication and form login. The CSRF protection is enabled,
     * and the logout URL is set to {@code /oauth/logout}. The logout handler is set to
     * {@link HeaderWriterLogoutHandler} with a {@link ClearSiteDataHeaderWriter} to clear
     * the cookies.
     *
     * @param http the {@link HttpSecurity} instance
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while configuring the {@link SecurityFilterChain}
     */
    @Bean
    public SecurityFilterChain springSecurity(HttpSecurity http) throws Exception {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName("continue");
        http.authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .anyRequest().authenticated())
                .securityContext((securityContext) -> securityContext.requireExplicitSave(true))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        .maximumSessions(1))
                .httpBasic(Customizer.withDefaults())
                .formLogin((formLogin) -> formLogin.loginPage("/login").permitAll())
                .csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .logout((logout) -> logout.logoutUrl("/oauth/logout")
                        .addLogoutHandler(new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(COOKIES))));
        return http.build();
    }
}